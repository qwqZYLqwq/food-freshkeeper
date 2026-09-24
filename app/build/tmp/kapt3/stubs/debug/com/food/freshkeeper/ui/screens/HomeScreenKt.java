package com.food.freshkeeper.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u0007\u001a\u0018\u0010\u0005\u001a\u00020\u00012\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\u0007\u001aR\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u00032\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00102\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0015H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u0016\u0010\u0017\u001a*\u0010\u0018\u001a\u00020\u00012\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001a2\u0012\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\u00010\u001dH\u0007\u001a,\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u001b2\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00132\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00010\u0013H\u0007\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006!"}, d2 = {"HomeHeaderGreeting", "", "activeCount", "", "urgentCount", "HomeScreen", "navController", "Landroidx/navigation/NavHostController;", "viewModel", "Lcom/food/freshkeeper/FoodViewModel;", "SpaceCard", "title", "", "emoji", "count", "bgColor", "Landroidx/compose/ui/graphics/Color;", "accentColor", "onClick", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "SpaceCard-ZkgLGzA", "(Ljava/lang/String;Ljava/lang/String;IJJLkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;)V", "StorageSpaceQuickGrid", "activeFoods", "", "Lcom/food/freshkeeper/data/FoodItem;", "onSpaceClick", "Lkotlin/Function1;", "UrgentFoodCard", "food", "onConsume", "app_debug"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavHostController navController, @org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.FoodViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void HomeHeaderGreeting(int activeCount, int urgentCount) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void UrgentFoodCard(@org.jetbrains.annotations.NotNull()
    com.food.freshkeeper.data.FoodItem food, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConsume) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StorageSpaceQuickGrid(@org.jetbrains.annotations.NotNull()
    java.util.List<com.food.freshkeeper.data.FoodItem> activeFoods, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSpaceClick) {
    }
}