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
    const val GITHUB_CONTRIBUTORS_URL = "https://github.com/qwqZYLqwq/food-freshkeeper/graphs/contributors"
    const val GITHUB_ISSUES_URL = "https://github.com/qwqZYLqwq/food-freshkeeper/issues"

    private var activeDownloadId: Long? = null

    suspend fun checkAndDownload(
        context: Context,
        onStart: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        withContext(Dispatchers.IO) {
            try {
                var downloadUrl = FALLBACK_APK_URL
                var releaseVersion = "v1.9.0"
                try {
                    val conn = (URL(GITHUB_REPO_API).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8000
                        readTimeout = 8000
                        setRequestProperty("Accept", "application/vnd.github.v3+json")
                    }
                    if (conn.responseCode in 200..299) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        conn.disconnect()
                        val json = JSONObject(response)
                        releaseVersion = json.optString("tag_name", "v1.9.0")
                        val assets = json.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    val assetUrl = asset.optString("browser_download_url")
                                    if (assetUrl.isNotBlank()) {
                                        downloadUrl = assetUrl
                                        break
                                    }
                                }
                            }
                        }
                    } else {
                        conn.disconnect()
                    }
                } catch (e: Exception) {
                    // 使用兜底发布地址
                }

                withContext(Dispatchers.Main) {
                    startDownload(context, downloadUrl, releaseVersion)
                    onStart(releaseVersion)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "下载启动失败")
                }
            }
        }
    }

    fun startDownload(context: Context, downloadUrl: String, releaseVersion: String) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (dm == null) {
            openInBrowser(context, downloadUrl)
            return
        }

        try {
            val fileName = "FoodFreshKeeper_${releaseVersion}.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("鲜食记 - 下载更新 ($releaseVersion)")
                setDescription("正在下载新版本安装包，进度可在通知栏查看...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadId = dm.enqueue(request)
            activeDownloadId = downloadId
            Toast.makeText(context, "已在通知栏启动下载更新 📥\n版本: $releaseVersion", Toast.LENGTH_LONG).show()

            registerDownloadCompleteReceiver(context.applicationContext, downloadId, fileName)
        } catch (e: Exception) {
            Toast.makeText(context, "下载服务调用失败，正在打开浏览器下载...", Toast.LENGTH_SHORT).show()
            openInBrowser(context, downloadUrl)
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
