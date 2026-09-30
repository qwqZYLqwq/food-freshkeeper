package com.food.freshkeeper.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 负责将食材原图流式保存到系统公共下载区 (Download/...)
 * 具备 Android 10+ (Scoped Storage / MediaStore) 与 Android 8~9 传统存储全兼容支持
 */
object ImageSaver {

    const val DEFAULT_IMAGE_SAVE_PATH = "Download/food"

    /**
     * 规范化并清洗保存目录：
     * 1. 强制限制在系统公共 Download 域内
     * 2. 剥离路径穿越字符 (..) 与非法文件系统字符
     * 3. 容错空值与无意义输入，回退至默认 "Download/food"
     */
    fun sanitizeImageSavePath(input: String?): String {
        if (input.isNullOrBlank()) {
            return DEFAULT_IMAGE_SAVE_PATH
        }

        // 统一斜杠分隔符
        var normalized = input.replace('\\', '/')

        // 剔除非法字符与控制字符
        normalized = normalized.replace(Regex("[:*?\"<>|\\u0000-\\u001F]"), "")

        // 过滤空段及跨目录穿越
        val segments = normalized.split('/')
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "." && it != ".." }

        if (segments.isEmpty()) {
            return DEFAULT_IMAGE_SAVE_PATH
        }

        // 规范化根目录为 Download
        val finalSegments = if (segments.first().equals("Download", ignoreCase = true)) {
            listOf("Download") + segments.drop(1)
        } else {
            listOf("Download") + segments
        }

        // 如果用户仅填写了 "Download"，默认补全子目录为 "Download/food"
        val result = if (finalSegments.size == 1 && finalSegments[0] == "Download") {
            DEFAULT_IMAGE_SAVE_PATH
        } else {
            finalSegments.joinToString("/")
        }

        return if (result.length > 120) {
            DEFAULT_IMAGE_SAVE_PATH
        } else {
            result
        }
    }

    /**
     * 将指定食材图片二进制流保存至公共下载目录
     *
     * @param context 应用程序上下文
     * @param sourceUriString 来源图片本地路径或 Uri (如 /data/user/0/.../food_123.jpg 或 file:// 或 content://)
     * @param foodName 食材名称，用于生成语义化文件名
     * @param targetRelativePath 目标相对路径 (如 "Download/food")
     * @return 成功返回展示用相对路径 (如 "Download/food/food_鲜草莓_20260930_123000.jpg")，失败返回异常
     */
    fun saveImage(
        context: Context,
        sourceUriString: String,
        foodName: String,
        targetRelativePath: String
    ): Result<String> {
        val inputStream = openSourceInputStream(context, sourceUriString)
            ?: return Result.failure(IOException("原图片文件不存在或已损坏"))

        val sanitizedPath = sanitizeImageSavePath(targetRelativePath)
        val safeFoodName = foodName.replace(Regex("[/\\\\:*?\"<>|\\s]"), "_").ifBlank { "food" }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val extension = when {
            sourceUriString.endsWith(".png", ignoreCase = true) -> "png"
            sourceUriString.endsWith(".webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val mimeType = when (extension) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val fileName = "food_${safeFoodName}_$timeStamp.$extension"
        val displayRelativePath = "$sanitizedPath/$fileName"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(
                    context = context,
                    inputStream = inputStream,
                    fileName = fileName,
                    mimeType = mimeType,
                    sanitizedPath = sanitizedPath,
                    displayRelativePath = displayRelativePath
                )
            } else {
                saveViaLegacyExternalStorage(
                    context = context,
                    inputStream = inputStream,
                    fileName = fileName,
                    sanitizedPath = sanitizedPath,
                    displayRelativePath = displayRelativePath
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Android 10+ (API 29+): Scoped Storage 模式
     * 写入 MediaStore.Downloads，无需任何运行时读写权限
     */
    private fun saveViaMediaStore(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        mimeType: String,
        sanitizedPath: String,
        displayRelativePath: String
    ): Result<String> {
        val resolver = context.contentResolver
        val mediaStoreRelativePath = if (sanitizedPath.endsWith("/")) sanitizedPath else "$sanitizedPath/"

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, mediaStoreRelativePath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Result.failure(IOException("无法在公共下载区创建媒体记录"))

        try {
            resolver.openOutputStream(itemUri)?.use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            } ?: run {
                resolver.delete(itemUri, null, null)
                return Result.failure(IOException("无法打开目标图片输出流"))
            }

            // 完成写入，解除待处理状态
            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, contentValues, null, null)

            // 发起 MediaScanner 扫描以确保相册与第三方文件管理器即时索引
            val storageRoot = Environment.getExternalStorageDirectory()
            val physicalFile = File(storageRoot, displayRelativePath)
            MediaScannerConnection.scanFile(
                context.applicationContext,
                arrayOf(physicalFile.absolutePath),
                arrayOf(mimeType),
                null
            )

            return Result.success(displayRelativePath)
        } catch (e: Exception) {
            try {
                resolver.delete(itemUri, null, null)
            } catch (_: Exception) {}
            return Result.failure(e)
        }
    }

    /**
     * Android 8.0 ~ 9.0 (API 26 ~ 28): 传统外部存储模式
     * 写入 /sdcard/Download/... 目录，配合 MediaScannerConnection 索引
     */
    private fun saveViaLegacyExternalStorage(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        sanitizedPath: String,
        displayRelativePath: String
    ): Result<String> {
        val subDir = sanitizedPath.removePrefix("Download/").removePrefix("Download").trim('/')
        val baseDownloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = if (subDir.isEmpty()) baseDownloadDir else File(baseDownloadDir, subDir)

        if (!targetDir.exists()) {
            val created = targetDir.mkdirs()
            if (!created && !targetDir.exists()) {
                return Result.failure(IOException("无法创建目标保存目录: ${targetDir.absolutePath}"))
            }
        }

        val targetFile = File(targetDir, fileName)
        FileOutputStream(targetFile).use { outputStream ->
            inputStream.use { input ->
                input.copyTo(outputStream)
            }
        }

        // 通知系统相册与文件管理器刷新索引
        MediaScannerConnection.scanFile(
            context.applicationContext,
            arrayOf(targetFile.absolutePath),
            arrayOf("image/jpeg"),
            null
        )

        return Result.success(displayRelativePath)
    }

    /**
     * 打开原始图片输入流，兼容真实本地文件路径、file:// 与 content:// 协议
     */
    private fun openSourceInputStream(context: Context, sourceUriString: String): InputStream? {
        return try {
            if (sourceUriString.startsWith("content://") || sourceUriString.startsWith("file://")) {
                val uri = Uri.parse(sourceUriString)
                context.contentResolver.openInputStream(uri)
            } else {
                val file = File(sourceUriString)
                if (file.exists() && file.canRead()) {
                    FileInputStream(file)
                } else {
                    val uri = Uri.parse(sourceUriString)
                    context.contentResolver.openInputStream(uri)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
