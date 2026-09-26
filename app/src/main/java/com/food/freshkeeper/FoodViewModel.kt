package com.food.freshkeeper

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.food.freshkeeper.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class SortOption(val title: String) {
    EXPIRY_ASC("最早到期优先 ⏳"),
    EXPIRY_DESC("最晚到期优先 📅"),
    ADDED_DESC("最新添加优先 🆕"),
    NAME_ASC("名称排序 🔤")
}

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val foodDao: FoodDao
    private val repository: FoodRepository
    private val settingsRepository = SettingsRepository(application)
    private val syncClient = FoodSyncClient()

    // 云端同步与设置状态流
    val serverUrl: StateFlow<String> = settingsRepository.serverUrl.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val autoSyncEnabled: StateFlow<Boolean> = settingsRepository.autoSyncEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val lastSyncTimeMs: StateFlow<Long> = settingsRepository.lastSyncTimeMs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0L
    )

    val defaultReminderDays: StateFlow<Int> = settingsRepository.defaultReminderDays.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        3
    )

    val isSyncing = MutableStateFlow(false)
    val isRefreshing = MutableStateFlow(false)

    val allFoods: StateFlow<List<FoodItem>>
    val activeFoods: StateFlow<List<FoodItem>>
    val consumedFoods: StateFlow<List<FoodItem>>
    val trashFoods: StateFlow<List<FoodItem>>

    // 搜索关键字与筛选
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("全部") // 全部、冷藏、冷冻、常温、紧急临期、已过期、已消灭
    val sortOption = MutableStateFlow(SortOption.EXPIRY_ASC)

    // 批量管理模式与选中集合
    val isBatchMode = MutableStateFlow(false)
    val selectedFoodIds = MutableStateFlow<Set<Long>>(emptySet())

    // 庆祝对话框触发食品名
    val celebratedFoodName = MutableStateFlow<String?>(null)

    init {
        val database = FoodDatabase.getDatabase(application, viewModelScope)
        foodDao = database.foodDao()
        repository = FoodRepository(foodDao)

        allFoods = repository.allFoods.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeFoods = repository.activeFoods.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        consumedFoods = repository.consumedFoods.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        trashFoods = repository.trashFoods.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // 临期食品 (根据设置中的预警阈值天数动态筛选)
    val urgentFoods: StateFlow<List<FoodItem>> = combine(
        activeFoods,
        defaultReminderDays
    ) { list, threshold ->
        list.filter { food ->
            val days = food.remainingDays()
            days in 0..threshold
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 已过期食品 (<0天)
    val expiredFoods: StateFlow<List<FoodItem>> = activeFoods.map { list ->
        list.filter { it.remainingDays() < 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 过滤与排序后的清单流
    val filteredListFoods: StateFlow<List<FoodItem>> = combine(
        allFoods,
        searchQuery,
        selectedFilter,
        sortOption,
        defaultReminderDays
    ) { foods, query, filter, sort, threshold ->
        val trimmedQuery = query.trim().lowercase()

        // 1. 状态与位置筛选
        val afterStatus = foods.filter { food ->
            when (filter) {
                "全部" -> !food.isConsumed
                "冷藏" -> !food.isConsumed && food.location.contains("冷藏")
                "冷冻" -> !food.isConsumed && food.location.contains("冷冻")
                "常温" -> !food.isConsumed && food.location.contains("常温")
                "紧急临期", "临期待吃" -> !food.isConsumed && food.remainingDays() in 0..threshold
                "已过期" -> !food.isConsumed && food.remainingDays() < 0
                "已消灭" -> food.isConsumed
                else -> true
            }
        }

        // 2. 关键字搜索
        val afterSearch = if (trimmedQuery.isEmpty()) {
            afterStatus
        } else {
            afterStatus.filter {
                it.name.lowercase().contains(trimmedQuery) ||
                it.category.lowercase().contains(trimmedQuery) ||
                it.notes.lowercase().contains(trimmedQuery)
            }
        }

        // 3. 排序
        when (sort) {
            SortOption.EXPIRY_ASC -> afterSearch.sortedBy { it.expiryDateMs }
            SortOption.EXPIRY_DESC -> afterSearch.sortedByDescending { it.expiryDateMs }
            SortOption.ADDED_DESC -> afterSearch.sortedByDescending { it.createdAtMs }
            SortOption.NAME_ASC -> afterSearch.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        selectedFilter.value = filter
    }

    fun setSortOption(sort: SortOption) {
        sortOption.value = sort
    }

    // 批量管理操作
    fun toggleBatchMode() {
        isBatchMode.value = !isBatchMode.value
        if (!isBatchMode.value) {
            selectedFoodIds.value = emptySet()
        }
    }

    fun exitBatchMode() {
        isBatchMode.value = false
        selectedFoodIds.value = emptySet()
    }

    fun toggleFoodSelection(id: Long) {
        val current = selectedFoodIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        selectedFoodIds.value = current
    }

    fun selectAll(ids: List<Long>) {
        if (selectedFoodIds.value.size == ids.size) {
            selectedFoodIds.value = emptySet()
        } else {
            selectedFoodIds.value = ids.toSet()
        }
    }

    fun deleteSelectedBatch() {
        val idsToDelete = selectedFoodIds.value.toList()
        if (idsToDelete.isNotEmpty()) {
            viewModelScope.launch {
                repository.moveToTrashBatch(idsToDelete)
                exitBatchMode()
                triggerAutoUploadIfEnabled()
            }
        }
    }

    fun addFood(food: FoodItem) {
        viewModelScope.launch {
            repository.insert(food)
            triggerAutoUploadIfEnabled()
        }
    }

    fun updateFood(food: FoodItem) {
        viewModelScope.launch {
            repository.update(food)
            triggerAutoUploadIfEnabled()
        }
    }

    // 单个移入回收站
    fun moveToTrash(food: FoodItem) {
        viewModelScope.launch {
            repository.moveToTrash(food.id)
            triggerAutoUploadIfEnabled()
        }
    }

    fun moveToTrashById(id: Long) {
        viewModelScope.launch {
            repository.moveToTrash(id)
            triggerAutoUploadIfEnabled()
        }
    }

    // 回收站恢复与彻底删除
    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
            triggerAutoUploadIfEnabled()
        }
    }

    fun restoreAllTrash() {
        viewModelScope.launch {
            repository.restoreAllTrash()
            triggerAutoUploadIfEnabled()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            triggerAutoUploadIfEnabled()
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            repository.deletePermanently(id)
            triggerAutoUploadIfEnabled()
        }
    }

    fun markConsumed(food: FoodItem) {
        viewModelScope.launch {
            repository.markConsumed(food)
            celebratedFoodName.value = food.name
            triggerAutoUploadIfEnabled()
        }
    }

    fun markActive(food: FoodItem) {
        viewModelScope.launch {
            repository.markActive(food)
            triggerAutoUploadIfEnabled()
        }
    }

    fun dismissCelebration() {
        celebratedFoodName.value = null
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetSampleData()
            triggerAutoUploadIfEnabled()
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            repository.addSampleData()
            triggerAutoUploadIfEnabled()
        }
    }

    fun clearConsumed() {
        viewModelScope.launch {
            repository.clearConsumed()
            triggerAutoUploadIfEnabled()
        }
    }

    fun clearExpired() {
        viewModelScope.launch {
            repository.clearExpired()
            triggerAutoUploadIfEnabled()
        }
    }

    private fun getImagesDir(): File {
        val context = getApplication<Application>()
        return File(context.filesDir, "food_images").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun triggerAutoUploadIfEnabled() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = serverUrl.value
                val enabled = autoSyncEnabled.value
                if (!enabled || url.isBlank()) return@launch
                val snapshot = foodDao.getAllFoodItemsSnapshot()
                val result = syncClient.uploadFoods(url, snapshot, getImagesDir())
                if (result.isSuccess) {
                    settingsRepository.setLastSyncTime(System.currentTimeMillis())
                }
            } catch (e: Exception) {
                // 离线优先：自动同步失败静默忽略，绝不中断用户体验
            }
        }
    }

    /**
     * 下拉刷新：重新刷新本地状态并在开启同步时静默同步
     */
    fun refreshData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                val url = serverUrl.value
                val enabled = autoSyncEnabled.value
                if (enabled && url.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        val imagesDir = getImagesDir()
                        val result = syncClient.fetchFoods(url, imagesDir)
                        if (result.isSuccess) {
                            val remoteFoods = result.getOrNull() ?: emptyList()
                            foodDao.insertAll(remoteFoods)
                            settingsRepository.setLastSyncTime(System.currentTimeMillis())
                        }
                    }
                }
            } catch (e: Exception) {
                // 静默忽略
            } finally {
                kotlinx.coroutines.delay(350)
                isRefreshing.value = false
                onComplete?.invoke()
            }
        }
    }

    // 设置与偏好操作
    fun setServerUrl(url: String) {
        viewModelScope.launch {
            settingsRepository.setServerUrl(url)
        }
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSyncEnabled(enabled)
        }
    }

    fun setDefaultReminderDays(days: Int) {
        viewModelScope.launch {
            settingsRepository.setDefaultReminderDays(days)
        }
    }

    // 手动网络同步操作
    fun testConnection(targetUrl: String? = null, onResult: (Boolean, String) -> Unit) {
        val urlToTest = targetUrl ?: serverUrl.value
        if (urlToTest.isBlank()) {
            onResult(false, "服务器地址不能为空")
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            val result = syncClient.testConnection(urlToTest)
            isSyncing.value = false
            if (result.isSuccess) {
                onResult(true, result.getOrDefault("连接成功"))
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "无法连接到服务器"
                onResult(false, errorMsg)
            }
        }
    }

    fun uploadBackup(onResult: (Boolean, String) -> Unit) {
        val url = serverUrl.value
        if (url.isBlank()) {
            onResult(false, "请先配置服务器地址")
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            try {
                val snapshot = withContext(Dispatchers.IO) { foodDao.getAllFoodItemsSnapshot() }
                val result = syncClient.uploadFoods(url, snapshot, getImagesDir())
                isSyncing.value = false
                if (result.isSuccess) {
                    val count = result.getOrDefault(snapshot.size)
                    val now = System.currentTimeMillis()
                    settingsRepository.setLastSyncTime(now)
                    onResult(true, "上传成功，共备份 $count 项食材 ☁️")
                } else {
                    val err = result.exceptionOrNull()?.message ?: "上传失败"
                    onResult(false, "上传失败: $err")
                }
            } catch (e: Exception) {
                isSyncing.value = false
                onResult(false, "上传异常: ${e.message}")
            }
        }
    }

    fun restoreFromCloud(onResult: (Boolean, String) -> Unit) {
        val url = serverUrl.value
        if (url.isBlank()) {
            onResult(false, "请先配置服务器地址")
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            try {
                val imagesDir = getImagesDir()
                val result = syncClient.fetchFoods(url, imagesDir)
                if (result.isSuccess) {
                    val remoteFoods = result.getOrNull() ?: emptyList()
                    withContext(Dispatchers.IO) {
                        foodDao.insertAll(remoteFoods)
                    }
                    val now = System.currentTimeMillis()
                    settingsRepository.setLastSyncTime(now)
                    isSyncing.value = false

                    val activeCount = remoteFoods.count { !it.isConsumed && !it.isDeleted }
                    val consumedCount = remoteFoods.count { it.isConsumed && !it.isDeleted }
                    val trashCount = remoteFoods.count { it.isDeleted }
                    val msg = "同步成功！已恢复 ${remoteFoods.size} 项食材 (在库 $activeCount 项, 已食用 $consumedCount 项, 回收站 $trashCount 项) 📥"
                    onResult(true, msg)
                } else {
                    isSyncing.value = false
                    val err = result.exceptionOrNull()?.message ?: "拉取失败"
                    onResult(false, "同步失败: $err")
                }
            } catch (e: Exception) {
                isSyncing.value = false
                onResult(false, "同步异常: ${e.message}")
            }
        }
    }

    // 本地离线备份包导出与导入
    fun exportBackup(destinationUri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                val context = getApplication<Application>()
                val snapshot = withContext(Dispatchers.IO) { foodDao.getAllFoodItemsSnapshot() }
                val result = BackupManager.exportBackup(context, destinationUri, snapshot)
                isSyncing.value = false
                onResult(result.success, result.message)
            } catch (e: Exception) {
                isSyncing.value = false
                onResult(false, "导出异常: ${e.message}")
            }
        }
    }

    fun importBackup(sourceUri: Uri, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                val context = getApplication<Application>()
                val (result, importedFoods) = BackupManager.importBackup(context, sourceUri)
                if (result.success && importedFoods.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        foodDao.insertAll(importedFoods)
                    }
                    triggerAutoUploadIfEnabled()
                }
                isSyncing.value = false
                onResult(result.success, result.message)
            } catch (e: Exception) {
                isSyncing.value = false
                onResult(false, "导入异常: ${e.message}")
            }
        }
    }

    fun getFoodById(id: Long): Flow<FoodItem?> = repository.getFoodById(id)

    /**
     * 将选中的外部图片保存到应用私有目录，确保永久有效
     */
    suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val imagesDir = getImagesDir()
            val targetFile = File(imagesDir, "food_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将拍照临时文件规范化保存到正式图片目录
     */
    suspend fun saveCapturedPhotoToStorage(tempFile: File): String? = withContext(Dispatchers.IO) {
        try {
            if (!tempFile.exists() || tempFile.length() == 0L) return@withContext null
            val imagesDir = getImagesDir()
            val targetFile = File(imagesDir, "food_${System.currentTimeMillis()}.jpg")
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 发送一条真实的食品过期模拟通知
     */
    fun sendTestExpiryNotification(context: android.content.Context): Pair<Boolean, String> {
        val expiredSample = expiredFoods.value.firstOrNull()
        val urgentSample = urgentFoods.value.firstOrNull()
        val activeSample = activeFoods.value.firstOrNull()

        val foodName = when {
            expiredSample != null -> "${expiredSample.name} ${expiredSample.iconEmoji}"
            urgentSample != null -> "${urgentSample.name} ${urgentSample.iconEmoji}"
            activeSample != null -> "${activeSample.name} ${activeSample.iconEmoji}"
            else -> "鲜牛奶 🥛"
        }

        val daysMessage = when {
            expiredSample != null -> "已过期 ${-expiredSample.remainingDays()} 天"
            urgentSample != null -> "仅剩 ${urgentSample.remainingDays()} 天即将过期"
            activeSample != null -> "仅剩 ${activeSample.remainingDays()} 天即将过期"
            else -> "已过期 1 天"
        }

        val location = expiredSample?.location ?: urgentSample?.location ?: activeSample?.location ?: "冷藏室 🧊"

        val success = NotificationHelper.sendExpiryNotification(
            context = context,
            foodName = foodName,
            daysMessage = daysMessage,
            location = location
        )

        return if (success) {
            Pair(true, "已成功发送测试通知，请下拉通知栏查看 🔔")
        } else {
            Pair(false, "通知发送受阻，请确保系统已允许鲜食记的通知权限")
        }
    }
}
