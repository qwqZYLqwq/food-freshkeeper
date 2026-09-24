package com.food.freshkeeper.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0010\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0010\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u0012H\'J\u0014\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u0012H\'J\u0014\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u0012H\'J\u000e\u0010\u0016\u001a\u00020\u0017H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0018\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u00122\u0006\u0010\u000f\u001a\u00020\u0007H\'J\u0014\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u0012H\'J\u001c\u0010\u001a\u001a\u00020\u00032\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u0016\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ \u0010\u001e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u00072\b\b\u0002\u0010\u001f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010 J&\u0010!\u001a\u00020\u00032\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00070\u00132\b\b\u0002\u0010\u001f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010#J\u000e\u0010$\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010%\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010&\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\r\u00a8\u0006\'"}, d2 = {"Lcom/food/freshkeeper/data/FoodDao;", "", "clearAll", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearConsumed", "nowMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearExpired", "deleteFood", "food", "Lcom/food/freshkeeper/data/FoodItem;", "(Lcom/food/freshkeeper/data/FoodItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePermanently", "id", "emptyTrash", "getActiveFoods", "Lkotlinx/coroutines/flow/Flow;", "", "getAllFoods", "getConsumedFoods", "getCount", "", "getFoodById", "getTrashFoods", "insertAll", "foods", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertFood", "moveToTrash", "deletedAtMs", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "moveToTrashBatch", "ids", "(Ljava/util/List;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "restoreAllTrash", "restoreFromTrash", "updateFood", "app_debug"})
@androidx.room.Dao()
public abstract interface FoodDao {
    
    @androidx.room.Query(value = "SELECT * FROM food_items WHERE isDeleted = 0 ORDER BY expiryDateMs ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getAllFoods();
    
    @androidx.room.Query(value = "SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 0 ORDER BY expiryDateMs ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getActiveFoods();
    
    @androidx.room.Query(value = "SELECT * FROM food_items WHERE isDeleted = 0 AND isConsumed = 1 ORDER BY consumedAtMs DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getConsumedFoods();
    
    @androidx.room.Query(value = "SELECT * FROM food_items WHERE isDeleted = 1 ORDER BY deletedAtMs DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getTrashFoods();
    
    @androidx.room.Query(value = "SELECT * FROM food_items WHERE id = :id")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.food.freshkeeper.data.FoodItem> getFoodById(long id);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM food_items WHERE isDeleted = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertFood(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<com.food.freshkeeper.data.FoodItem> foods, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateFood(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteFood(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 1, deletedAtMs = :deletedAtMs WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object moveToTrash(long id, long deletedAtMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 1, deletedAtMs = :deletedAtMs WHERE id IN (:ids)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object moveToTrashBatch(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Long> ids, long deletedAtMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restoreFromTrash(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 0, deletedAtMs = NULL WHERE isDeleted = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restoreAllTrash(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM food_items WHERE isDeleted = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object emptyTrash(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM food_items WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePermanently(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 1, deletedAtMs = :nowMs WHERE isConsumed = 1 AND isDeleted = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearConsumed(long nowMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE food_items SET isDeleted = 1, deletedAtMs = :nowMs WHERE isConsumed = 0 AND isDeleted = 0 AND expiryDateMs < :nowMs")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearExpired(long nowMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM food_items")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object clearAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}