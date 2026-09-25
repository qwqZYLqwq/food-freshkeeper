package com.food.freshkeeper.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    val allFoods: Flow<List<FoodItem>> = foodDao.getAllFoods()
    val activeFoods: Flow<List<FoodItem>> = foodDao.getActiveFoods()
    val consumedFoods: Flow<List<FoodItem>> = foodDao.getConsumedFoods()
    val trashFoods: Flow<List<FoodItem>> = foodDao.getTrashFoods()

    fun getFoodById(id: Long): Flow<FoodItem?> = foodDao.getFoodById(id)

    suspend fun insert(food: FoodItem): Long = foodDao.insertFood(food)

    suspend fun update(food: FoodItem) = foodDao.updateFood(food)

    // 单个移入回收站
    suspend fun moveToTrash(id: Long) = foodDao.moveToTrash(id)

    // 批量移入回收站
    suspend fun moveToTrashBatch(ids: List<Long>) = foodDao.moveToTrashBatch(ids)

    // 恢复
    suspend fun restoreFromTrash(id: Long) = foodDao.restoreFromTrash(id)

    suspend fun restoreAllTrash() = foodDao.restoreAllTrash()

    // 彻底清空回收站
    suspend fun emptyTrash() = foodDao.emptyTrash()

    // 彻底删除单个
    suspend fun deletePermanently(id: Long) = foodDao.deletePermanently(id)

    suspend fun markConsumed(food: FoodItem) {
        val updated = food.copy(
            isConsumed = true,
            consumedAtMs = System.currentTimeMillis()
        )
        foodDao.updateFood(updated)
    }

    suspend fun markActive(food: FoodItem) {
        val updated = food.copy(
            isConsumed = false,
            consumedAtMs = null
        )
        foodDao.updateFood(updated)
    }

    suspend fun resetSampleData() {
        foodDao.clearAll()
        foodDao.insertAll(FoodDataPresets.createInitialMockFoods())
    }

    suspend fun addSampleData() {
        // 生成全新 ID 的示范食材以避免覆盖已有食材
        val samples = FoodDataPresets.createInitialMockFoods().map { it.copy(id = 0L) }
        foodDao.insertAll(samples)
    }

    suspend fun clearConsumed() {
        foodDao.clearConsumed()
    }

    suspend fun clearExpired() {
        foodDao.clearExpired(System.currentTimeMillis())
    }
}
