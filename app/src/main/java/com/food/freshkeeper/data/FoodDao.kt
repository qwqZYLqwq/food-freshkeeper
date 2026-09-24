package com.food.freshkeeper.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM food_items WHERE isDeleted = 0 ORDER BY expiryDateMs ASC")
    fun getAllFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 0 ORDER BY expiryDateMs ASC")
    fun getActiveFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 1 ORDER BY consumedAtMs DESC")
    fun getConsumedFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isDeleted = 1 ORDER BY deletedAtMs DESC")
    fun getTrashFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun getFoodById(id: Long): Flow<FoodItem?>

    @Query("SELECT COUNT(*) FROM food_items WHERE isDeleted = 0")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foods: List<FoodItem>)

    @Update
    suspend fun updateFood(food: FoodItem)

    @Delete
    suspend fun deleteFood(food: FoodItem)

    // 移入回收站 (软删除)
    @Query("UPDATE food_items SET isDeleted = 1, deletedAtMs = :deletedAtMs WHERE id = :id")
    suspend fun moveToTrash(id: Long, deletedAtMs: Long = System.currentTimeMillis())

    // 批量移入回收站
    @Query("UPDATE food_items SET isDeleted = 1, deletedAtMs = :deletedAtMs WHERE id IN (:ids)")
    suspend fun moveToTrashBatch(ids: List<Long>, deletedAtMs: Long = System.currentTimeMillis())

    // 从回收站恢复
    @Query("UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    // 全部恢复
    @Query("UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE isDeleted = 1")
    suspend fun restoreAllTrash()

    // 彻底清空回收站 (硬删除)
    @Query("DELETE FROM food_items WHERE isDeleted = 1")
    suspend fun emptyTrash()

    // 彻底删除单项
    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("UPDATE food_items SET isDeleted = 1, deletedAtMs = :nowMs WHERE isConsumed = 1 AND isDeleted = 0")
    suspend fun clearConsumed(nowMs: Long = System.currentTimeMillis())

    @Query("UPDATE food_items SET isDeleted = 1, deletedAtMs = :nowMs WHERE isConsumed = 0 AND isDeleted = 0 AND expiryDateMs < :nowMs")
    suspend fun clearExpired(nowMs: Long)

    @Query("DELETE FROM food_items")
    suspend fun clearAll()
}
