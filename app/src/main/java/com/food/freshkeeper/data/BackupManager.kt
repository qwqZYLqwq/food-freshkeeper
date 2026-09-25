package com.food.freshkeeper.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupResult(
    val success: Boolean,
    val message: String,
    val foodCount: Int = 0,
    val imageCount: Int = 0
)

object BackupManager {

    suspend fun exportBackup(
        context: Context,
        destinationUri: Uri,
        foods: List<FoodItem>
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, "food_images")
            val outputStream = context.contentResolver.openOutputStream(destinationUri)
                ?: return@withContext BackupResult(false, "无法创建备份文件输出流")

            var imageCount = 0
            val exportedFoods = mutableListOf<JSONObject>()
            val syncClient = FoodSyncClient()
            val writtenEntries = mutableSetOf<String>()

            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. 打包所有食材关联的本地图片并去重
                foods.forEach { food ->
                    // 导出离线包时排除冗余的 imageBase64，节省体积
                    val foodObj = syncClient.foodToJson(food, imagesDir, includeBase64 = false)
                    foodObj.remove("imageBase64")
                    foodObj.remove("imageFileName")

                    val localUri = food.imageUri
                    if (!localUri.isNullOrBlank()) {
                        var path = localUri
                        if (path.startsWith("file://")) {
                            path = path.removePrefix("file://")
                        }
                        val imgFile = File(path)
                        if (imgFile.exists() && imgFile.isFile) {
                            val entryName = "images/" + imgFile.name
                            if (!writtenEntries.contains(entryName)) {
                                zipOut.putNextEntry(ZipEntry(entryName))
                                FileInputStream(imgFile).use { fis ->
                                    fis.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                                writtenEntries.add(entryName)
                                imageCount++
                            }
                            // 记录相对文件名
                            foodObj.put("relativeImagePath", entryName)
                        }
                    }
                    exportedFoods.add(foodObj)
                }

                // 2. 打包 JSON 元数据
                val rootJson = JSONObject().apply {
                    put("version", "1.3.0")
                    put("appName", "鲜食记")
                    put("exportTimeMs", System.currentTimeMillis())
                    put("count", foods.size)
                    val array = JSONArray()
                    exportedFoods.forEach { array.put(it) }
                    put("foods", array)
                }

                zipOut.putNextEntry(ZipEntry("backup_data.json"))
                val jsonBytes = rootJson.toString(2).toByteArray(Charsets.UTF_8)
                zipOut.write(jsonBytes)
                zipOut.closeEntry()
            }

            BackupResult(
                success = true,
                message = "导出成功！已打包 ${foods.size} 项食材与 $imageCount 张图片 📦",
                foodCount = foods.size,
                imageCount = imageCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult(false, "导出失败: ${e.message}")
        }
    }

    suspend fun importBackup(
        context: Context,
        sourceUri: Uri
    ): Pair<BackupResult, List<FoodItem>> = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, "food_images").apply {
                if (!exists()) mkdirs()
            }
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return@withContext Pair(BackupResult(false, "无法读取备份文件"), emptyList())

            var jsonContent: String? = null
            val extractedImages = mutableMapOf<String, String>() // relativeName -> absolutePath
            var imageCount = 0

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val name = entry.name
                    if (!entry.isDirectory && name.isNotBlank()) {
                        if (name == "backup_data.json" || name.endsWith(".json")) {
                            jsonContent = zipIn.bufferedReader(Charsets.UTF_8).readText()
                        } else if (name.startsWith("images/") || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".webp")) {
                            val fileName = File(name).name
                            if (fileName.isNotBlank()) {
                                val targetFile = File(imagesDir, "imported_${System.currentTimeMillis()}_$fileName")
                                FileOutputStream(targetFile).use { fos ->
                                    zipIn.copyTo(fos)
                                }
                                extractedImages[name] = targetFile.absolutePath
                                extractedImages[fileName] = targetFile.absolutePath
                                imageCount++
                            }
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            if (jsonContent == null) {
                return@withContext Pair(BackupResult(false, "备份文件中未找到有效的 backup_data.json"), emptyList())
            }

            val syncClient = FoodSyncClient()
            val resultFoods = mutableListOf<FoodItem>()

            val root = JSONObject(jsonContent!!)
            val foodsArray = if (root.has("foods")) root.getJSONArray("foods") else JSONArray(jsonContent)

            for (i in 0 until foodsArray.length()) {
                val obj = foodsArray.getJSONObject(i)
                val baseFood = syncClient.jsonToFood(obj, imagesDir)

                // 优先映射解压出的相对路径图片
                val relativePath = obj.optString("relativeImagePath", "")
                val finalImageUri = if (relativePath.isNotBlank() && extractedImages.containsKey(relativePath)) {
                    extractedImages[relativePath]
                } else if (!baseFood.imageUri.isNullOrBlank() && extractedImages.containsKey(File(baseFood.imageUri).name)) {
                    extractedImages[File(baseFood.imageUri).name]
                } else {
                    baseFood.imageUri
                }

                // 安全合并：分配新 ID (id = 0L) 以便 Room 自动自增，防止覆盖已有食材
                resultFoods.add(baseFood.copy(id = 0L, imageUri = finalImageUri))
            }

            Pair(
                BackupResult(
                    success = true,
                    message = "导入成功！已恢复 ${resultFoods.size} 项食材与 $imageCount 张图片 🎉",
                    foodCount = resultFoods.size,
                    imageCount = imageCount
                ),
                resultFoods
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(BackupResult(false, "导入失败: ${e.message}"), emptyList())
        }
    }
}
