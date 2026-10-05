package com.food.freshkeeper.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object UpdateManager {

    private const val GITHUB_REPO_API = "https://api.github.com/repos/qwqZYLqwq/food-freshkeeper/releases/latest"
    private const val FALLBACK_APK_URL = "https://github.com/qwqZYLqwq/food-freshkeeper/releases/latest/download/FoodFreshKeeper_v1.9.0.apk"
    const val GITHUB_RELEASE_PAGE = "https://github.com/qwqZYLqwq/food-freshkeeper/releases"
    const val GITHUB_PROFILE_URL = "https://github.com/qwqZYLqwq"
    const val GITHUB_REPO_URL = "https://github.com/qwqZYLqwq/food-freshkeeper"

    // GitHub 国内高速加速镜像前缀
    const val GH_PROXY_PREFIX = "https://ghproxy.net/"
    const val GH_FAST_PREFIX = "https://ghfast.top/"

    private var activeDownloadId: Long? = null

    data class UpdateCheckInfo(
        val isLatest: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val acceleratedDownloadUrl: String,
        val rawDownloadUrl: String,
        val releaseNotes: String
    )

    /**
     * 版本号比较：检查 remote 是否比 current 新
     */
    fun isNewerVersion(current: String, remote: String): Boolean {
        val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")
        val cleanRemote = remote.trim().removePrefix("v").removePrefix("V")

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val remoteParts = cleanRemote.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(currentParts.size, remoteParts.size)
        for (i in 0 until maxLen) {
            val curr = currentParts.getOrElse(i) { 0 }
            val rem = remoteParts.getOrElse(i) { 0 }
            if (rem > curr) return true
            if (rem < curr) return false
        }
        return false
    }

    /**
     * 将标准 GitHub 下载链接转换为高速加速路线
     */
    fun getAcceleratedUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return "${GH_PROXY_PREFIX}$trimmed"
        }
        return "${GH_PROXY_PREFIX}https://github.com/$trimmed"
    }

    /**
     * 检测云端版本并返回新版本信息与加速下载直链
     */
    suspend fun checkVersion(currentVersion: String = "1.9.0"): Result<UpdateCheckInfo> = withContext(Dispatchers.IO) {
        try {
            var rawDownloadUrl = FALLBACK_APK_URL
            var releaseVersion = "v1.9.0"
            var releaseNotes = ""

            val conn = (URL(GITHUB_REPO_API).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "FoodFreshKeeper-App")
            }

            if (conn.responseCode in 200..299) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val json = JSONObject(response)
                releaseVersion = json.optString("tag_name", "v1.9.0")
                releaseNotes = json.optString("body", "常规体验优化与性能改进")

                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            val assetUrl = asset.optString("browser_download_url")
                            if (assetUrl.isNotBlank()) {
                                rawDownloadUrl = assetUrl
                                break
                            }
                        }
                    }
                }
            } else {
                conn.disconnect()
            }

            val hasNew = isNewerVersion(currentVersion, releaseVersion)
            val acceleratedUrl = getAcceleratedUrl(rawDownloadUrl)

            Result.success(
                UpdateCheckInfo(
                    isLatest = !hasNew,
                    currentVersion = currentVersion,
                    latestVersion = releaseVersion,
                    acceleratedDownloadUrl = acceleratedUrl,
                    rawDownloadUrl = rawDownloadUrl,
                    releaseNotes = releaseNotes
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 调起系统 DownloadManager 通过 GitHub 加速路线下载更新并在通知栏显示进度
     */
    fun startAcceleratedDownload(context: Context, acceleratedUrl: String, releaseVersion: String) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (dm == null) {
            openInBrowser(context, acceleratedUrl)
            return
        }

        try {
            val fileName = "FoodFreshKeeper_${releaseVersion}.apk"
            val request = DownloadManager.Request(Uri.parse(acceleratedUrl)).apply {
                setTitle("鲜食记 - 下载更新 ($releaseVersion)")
                setDescription("已启用 GitHub 高速加速路线，下载进度可在通知栏查看...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadId = dm.enqueue(request)
            activeDownloadId = downloadId
            Toast.makeText(context, "已通过 GitHub 加速路线启动下载 ⚡\n进度请在通知栏查看", Toast.LENGTH_LONG).show()

            registerDownloadCompleteReceiver(context.applicationContext, downloadId, fileName)
        } catch (e: Exception) {
            Toast.makeText(context, "下载服务调用异常，正在为您打开浏览器下载...", Toast.LENGTH_SHORT).show()
            openInBrowser(context, acceleratedUrl)
        }
    }

    private fun registerDownloadCompleteReceiver(appContext: Context, downloadId: Long, fileName: String) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
                if (id == downloadId) {
                    try {
                        appContext.unregisterReceiver(this)
                    } catch (e: Exception) {
                        // ignore
                    }

                    Toast.makeText(appContext, "更新包下载完成，正在调起安装...", Toast.LENGTH_SHORT).show()
                    installApk(appContext, downloadId, fileName)
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            appContext.registerReceiver(receiver, filter)
        }
    }

    private fun installApk(context: Context, downloadId: Long, fileName: String) {
        try {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val fileUri = dm.getUriForDownloadedFile(downloadId)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val apkFile = File(downloadsDir, fileName)
                if (apkFile.exists()) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(installIntent)
                }
            } catch (ex: Exception) {
                Toast.makeText(context, "下载已保存在系统的「下载」文件夹中，请在文件管理中点击安装", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开浏览器链接: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
