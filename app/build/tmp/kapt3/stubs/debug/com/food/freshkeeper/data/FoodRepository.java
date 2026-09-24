package com.food.freshkeeper.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\u0012\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0011\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010\u0014\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010\u0019\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010\u001a\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u001b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u00062\u0006\u0010\u0016\u001a\u00020\u0017J\u0016\u0010\u001c\u001a\u00020\u00172\u0006\u0010\u001d\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001eJ\u0016\u0010\u001f\u001a\u00020\u00122\u0006\u0010\u001d\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001eJ\u0016\u0010 \u001a\u00020\u00122\u0006\u0010\u001d\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001eJ\u0016\u0010!\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u001c\u0010\"\u001a\u00020\u00122\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00170\u0007H\u0086@\u00a2\u0006\u0002\u0010$J\u000e\u0010%\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u000e\u0010&\u001a\u00020\u0012H\u0086@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\'\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u0017H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010(\u001a\u00020\u00122\u0006\u0010\u001d\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001eR\u001d\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\n\u00a8\u0006)"}, d2 = {"Lcom/food/freshkeeper/data/FoodRepository;", "", "foodDao", "Lcom/food/freshkeeper/data/FoodDao;", "(Lcom/food/freshkeeper/data/FoodDao;)V", "activeFoods", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/food/freshkeeper/data/FoodItem;", "getActiveFoods", "()Lkotlinx/coroutines/flow/Flow;", "allFoods", "getAllFoods", "consumedFoods", "getConsumedFoods", "trashFoods", "getTrashFoods", "clearConsumed", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "clearExpired", "deletePermanently", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "emptyTrash", "ensureInitialData", "getFoodById", "insert", "food", "(Lcom/food/freshkeeper/data/FoodItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markActive", "markConsumed", "moveToTrash", "moveToTrashBatch", "ids", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetSampleData", "restoreAllTrash", "restoreFromTrash", "update", "app_debug"})
public final class FoodRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.food.freshkeeper.data.FoodDao foodDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> allFoods = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> activeFoods = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> consumedFoods = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> trashFoods = null;
    
    public FoodRepository(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodDao foodDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getAllFoods() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getActiveFoods() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getConsumedFoods() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.food.freshkeeper.data.FoodItem>> getTrashFoods() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<com.food.freshkeeper.data.FoodItem> getFoodById(long id) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object moveToTrash(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object moveToTrashBatch(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Long> ids, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object restoreFromTrash(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object restoreAllTrash(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object emptyTrash(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deletePermanently(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markConsumed(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object markActive(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object resetSampleData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearConsumed(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearExpired(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object ensureInitialData(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}