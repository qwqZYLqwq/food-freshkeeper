package com.food.freshkeeper.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FoodSyncClient(
    private val connectTimeoutMs: Int = 10000,
    private val readTimeoutMs: Int = 45000
) {

    private fun cleanBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim().removeSuffix("/")
        return when {
            trimmed.isEmpty() -> ""
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> trimmed
            else -> "http://$trimmed"
        }
    }

    suspend fun testConnection(serverUrl: String): Result<String> = withContext(Dispatchers.IO) {
        val baseUrl = cleanBaseUrl(serverUrl)
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("服务器地址为空"))
        }

        try {
            val healthUrl = "$baseUrl/api/health"
            val conn = (URL(healthUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = connectTimeoutMs
                setRequestProperty("Accept", "application/json")
                instanceFollowRedirects = true
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                conn.disconnect()
                val json = JSONObject(responseText)
                val service = json.optString("service", "鲜食记同步服务")
                val version = json.optString("version", "1.3.0")
                Result.success("服务在线: $service (v$version)")
            } else {
                conn.disconnect()
                val pingUrl = "$baseUrl/api/ping"
                val pingConn = (URL(pingUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = connectTimeoutMs
                    readTimeout = connectTimeoutMs
                    setRequestProperty("Accept", "application/json")
                    instanceFollowRedirects = true
                }
                val pingCode = pingConn.responseCode
                if (pingCode in 200..299) {
                    val pingText = pingConn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    pingConn.disconnect()
                    val json = JSONObject(pingText)
                    val message = json.optString("message", "连接成功")
                    Result.success(message)
                } else {
                    pingConn.disconnect()
                    Result.failure(Exception("HTTP $responseCode"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFoods(
        serverUrl: String,
        foods: List<FoodItem>,
        imagesDir: File? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        val baseUrl = cleanBaseUrl(serverUrl)
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("服务器地址为空"))
        }

        try {
            val targetUrl = "$baseUrl/api/foods"
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
            }

            val rootJson = JSONObject()
            val arrayJson = JSONArray()
            foods.forEach { food ->
                arrayJson.put(foodToJson(food, imagesDir))
            }
            rootJson.put("foods", arrayJson)
            rootJson.put("timestamp", System.currentTimeMillis())
            rootJson.put("count", foods.size)

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(rootJson.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                conn.disconnect()
                val respJson = JSONObject(responseText)
                val count = respJson.optInt("count", foods.size)
                Result.success(count)
            } else {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorText = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                conn.disconnect()
                Result.failure(Exception("HTTP $responseCode: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFoods(
        serverUrl: String,
        imagesDir: File? = null
    ): Result<List<FoodItem>> = withContext(Dispatchers.IO) {
        val baseUrl = cleanBaseUrl(serverUrl)
        if (baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("服务器地址为空"))
        }

        try {
            val targetUrl = "$baseUrl/api/foods"
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                setRequestProperty("Accept", "application/json")
                instanceFollowRedirects = true
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                conn.disconnect()

                val resultList = mutableListOf<FoodItem>()
                val trimmedText = responseText.trim()
                if (trimmedText.startsWith("[")) {
                    val jsonArray = JSONArray(trimmedText)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        resultList.add(jsonToFood(obj, imagesDir))
                    }
                } else {
                    val rootJson = JSONObject(trimmedText)
                    val foodsArray = rootJson.optJSONArray("foods")
                    if (foodsArray != null) {
                        for (i in 0 until foodsArray.length()) {
                            val obj = foodsArray.getJSONObject(i)
                            resultList.add(jsonToFood(obj, imagesDir))
                        }
                    }
                }
                Result.success(resultList)
            } else {
                val errorStream = conn.errorStream ?: conn.inputStream
                val errorText = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                conn.disconnect()
                Result.failure(Exception("HTTP $responseCode: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun foodToJson(food: FoodItem, imagesDir: File? = null, includeBase64: Boolean = true): JSONObject {
        val obj = JSONObject()
        obj.put("id", food.id)
        obj.put("name", food.name)
        obj.put("category", food.category)
        obj.put("iconEmoji", food.iconEmoji)
        obj.put("location", food.location)
        obj.put("productionDateMs", food.productionDateMs)
        obj.put("shelfLifeDays", food.shelfLifeDays)
        obj.put("expiryDateMs", food.expiryDateMs)
        obj.put("quantity", food.quantity)
        obj.put("notes", food.notes)
        
        // 处理图片路径与 Base64 编码
        if (!food.imageUri.isNullOrBlank()) {
            obj.put("imageUri", food.imageUri)
            if (includeBase64) {
                var path = food.imageUri
                if (path.startsWith("file://")) {
                    path = path.removePrefix("file://")
                }
                val imgFile = File(path)
                if (imgFile.exists() && imgFile.isFile) {
                    val base64 = compressFileToBase64(imgFile)
                    if (!base64.isNullOrBlank()) {
                        obj.put("imageBase64", base64)
                        obj.put("imageFileName", imgFile.name)
                    }
                }
            }
        } else {
            obj.put("imageUri", JSONObject.NULL)
        }

        obj.put("isConsumed", food.isConsumed)
        if (food.consumedAtMs != null) {
            obj.put("consumedAtMs", food.consumedAtMs)
        } else {
            obj.put("consumedAtMs", JSONObject.NULL)
        }
        obj.put("isDeleted", food.isDeleted)
        if (food.deletedAtMs != null) {
            obj.put("deletedAtMs", food.deletedAtMs)
        } else {
            obj.put("deletedAtMs", JSONObject.NULL)
        }
        obj.put("reminderDaysBefore", food.reminderDaysBefore)
        obj.put("createdAtMs", food.createdAtMs)
        return obj
    }

    fun jsonToFood(obj: JSONObject, imagesDir: File? = null): FoodItem {
        val id = obj.optLong("id", 0L)
        val name = obj.optString("name", "未命名食材")
        val category = obj.optString("category", "其他")
        val iconEmoji = obj.optString("iconEmoji", "🍲")
        val location = obj.optString("location", "冷藏室 🧊")
        val productionDateMs = obj.optLong("productionDateMs", System.currentTimeMillis())
        val shelfLifeDays = obj.optInt("shelfLifeDays", 3)
        val expiryDateMs = obj.optLong("expiryDateMs", System.currentTimeMillis())
        val quantity = obj.optString("quantity", "1份")
        val notes = obj.optString("notes", "")

        var imageUri: String? = if (obj.has("imageUri") && !obj.isNull("imageUri")) {
            val s = obj.getString("imageUri")
            if (s.isEmpty() || s == "null") null else s
        } else null

        // 如果存在 Base64 图片数据且本地目标文件不存在，解码保存为本地文件
        if (obj.has("imageBase64") && !obj.isNull("imageBase64") && imagesDir != null) {
            val base64 = obj.getString("imageBase64")
            if (base64.isNotBlank()) {
                val fileName = obj.optString("imageFileName", "food_sync_${id}_${System.currentTimeMillis()}.jpg")
                val localFile = File(imagesDir, fileName)
                val success = saveBase64ToFile(base64, localFile)
                if (success) {
                    imageUri = localFile.absolutePath
                }
            }
        }

        val isConsumed = obj.optBoolean("isConsumed", false)
        val consumedAtMs = if (obj.has("consumedAtMs") && !obj.isNull("consumedAtMs")) {
            val v = obj.getLong("consumedAtMs")
            if (v > 0) v else null
        } else null
        val isDeleted = obj.optBoolean("isDeleted", false)
        val deletedAtMs = if (obj.has("deletedAtMs") && !obj.isNull("deletedAtMs")) {
            val v = obj.getLong("deletedAtMs")
            if (v > 0) v else null
        } else null
        val reminderDaysBefore = obj.optInt("reminderDaysBefore", 3)
        val createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis())

        return FoodItem(
            id = id,
            name = name,
            category = category,
            iconEmoji = iconEmoji,
            location = location,
            productionDateMs = productionDateMs,
            shelfLifeDays = shelfLifeDays,
            expiryDateMs = expiryDateMs,
            quantity = quantity,
            notes = notes,
            imageUri = imageUri,
            isConsumed = isConsumed,
            consumedAtMs = consumedAtMs,
            isDeleted = isDeleted,
            deletedAtMs = deletedAtMs,
            reminderDaysBefore = reminderDaysBefore,
            createdAtMs = createdAtMs
        )
    }

    private fun compressFileToBase64(file: File, maxDimension: Int = 800, quality: Int = 80): String? {
        return try {
            var filePath = file.absolutePath
            if (filePath.startsWith("file://")) {
                filePath = filePath.removePrefix("file://")
            }
            val targetFile = File(filePath)
            if (!targetFile.exists() || !targetFile.isFile) return null

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(targetFile.absolutePath, options)
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDimension * 1.5 || options.outHeight / sampleSize > maxDimension * 1.5) {
                sampleSize *= 2
            }
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val bitmap = BitmapFactory.decodeFile(targetFile.absolutePath, decodeOptions) ?: return null

            val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
                val newHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                if (scaled != bitmap) {
                    bitmap.recycle()
                }
                scaled
            } else {
                bitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            scaledBitmap.recycle()
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBase64ToFile(base64Str: String, targetFile: File): Boolean {
        return try {
            val bytes = Base64.decode(base64Str, Base64.DEFAULT)
            if (targetFile.parentFile != null && !targetFile.parentFile!!.exists()) {
                targetFile.parentFile!!.mkdirs()
            }
            FileOutputStream(targetFile).use { fos ->
                fos.write(bytes)
                fos.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
