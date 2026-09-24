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

    private val repository: FoodRepository

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
        repository = FoodRepository(database.foodDao())

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

        // 确保初次安装有生动示范数据
        viewModelScope.launch {
            repository.ensureInitialData()
        }
    }

    // 临期食品 (<=2天且未过期)
    val urgentFoods: StateFlow<List<FoodItem>> = activeFoods.map { list ->
        list.filter {
            val days = it.remainingDays()
            days in 0..2
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
        sortOption
    ) { foods, query, filter, sort ->
        val trimmedQuery = query.trim().lowercase()

        // 1. 状态与位置筛选
        val afterStatus = foods.filter { food ->
            when (filter) {
                "全部" -> !food.isConsumed
                "冷藏" -> !food.isConsumed && food.location.contains("冷藏")
                "冷冻" -> !food.isConsumed && food.location.contains("冷冻")
                "常温" -> !food.isConsumed && food.location.contains("常温")
                "紧急临期" -> !food.isConsumed && food.remainingDays() in 0..2
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
            }
        }
    }

    fun addFood(food: FoodItem) {
        viewModelScope.launch {
            repository.insert(food)
        }
    }

    fun updateFood(food: FoodItem) {
        viewModelScope.launch {
            repository.update(food)
        }
    }

    // 单个移入回收站
    fun moveToTrash(food: FoodItem) {
        viewModelScope.launch {
            repository.moveToTrash(food.id)
        }
    }

    fun moveToTrashById(id: Long) {
        viewModelScope.launch {
            repository.moveToTrash(id)
        }
    }

    // 回收站恢复与彻底删除
    fun restoreFromTrash(id: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
        }
    }

    fun restoreAllTrash() {
        viewModelScope.launch {
            repository.restoreAllTrash()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            repository.deletePermanently(id)
        }
    }

    fun markConsumed(food: FoodItem) {
        viewModelScope.launch {
            repository.markConsumed(food)
            celebratedFoodName.value = food.name
        }
    }

    fun markActive(food: FoodItem) {
        viewModelScope.launch {
            repository.markActive(food)
        }
    }

    fun dismissCelebration() {
        celebratedFoodName.value = null
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetSampleData()
        }
    }

    fun clearConsumed() {
        viewModelScope.launch {
            repository.clearConsumed()
        }
    }

    fun clearExpired() {
        viewModelScope.launch {
            repository.clearExpired()
        }
    }

    fun getFoodById(id: Long): Flow<FoodItem?> = repository.getFoodById(id)

    /**
     * 将选中的外部图片保存到应用私有目录，确保永久有效
     */
    suspend fun saveImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val context = getApplication<Application>()
            val imagesDir = File(context.filesDir, "food_images").apply {
                if (!exists()) mkdirs()
            }
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
}
