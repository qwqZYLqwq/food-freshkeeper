package com.food.freshkeeper.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.data.FoodItem
import com.food.freshkeeper.ui.components.*
import com.food.freshkeeper.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val context = LocalContext.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000L) {
            (context as? Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            Toast.makeText(context, "再按一次退出鲜食记", Toast.LENGTH_SHORT).show()
        }
    }

    val activeFoods by viewModel.activeFoods.collectAsState()
    val urgentFoods by viewModel.urgentFoods.collectAsState()
    val expiredFoods by viewModel.expiredFoods.collectAsState()
    val consumedFoods by viewModel.consumedFoods.collectAsState()
    val trashFoods by viewModel.trashFoods.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshData {
                pullToRefreshState.endRefresh()
            }
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    // 庆祝弹窗
    val celebratedName by viewModel.celebratedFoodName.collectAsState()
    celebratedName?.let { name ->
        CelebrationDialog(
            foodName = name,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit") },
                shape = RoundedCornerShape(20.dp),
                containerColor = FreshGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加食品",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // 1. 顶部生动问候与标题
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HomeHeaderGreeting(activeCount = activeFoods.size, urgentCount = urgentFoods.size)
            }

            // 2. 冰箱新鲜健康概览卡片 (增加回收站指标入口)
            item {
                FridgeHealthOverviewCard(
                    activeCount = activeFoods.size,
                    urgentCount = urgentFoods.size,
                    expiredCount = expiredFoods.size,
                    consumedCount = consumedFoods.size,
                    trashCount = trashFoods.size,
                    onUrgentClick = {
                        viewModel.setSelectedFilter("紧急临期")
                        navController.navigate("list") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onTrashClick = {
                        navController.navigate("trash")
                    }
                )
            }

            // 3. 紧急消灭专区 (如果存在临期食品)
            if (urgentFoods.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔥 抓紧消灭专区",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = UrgentRed
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(UrgentRedBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${urgentFoods.size}件临期",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = UrgentRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 水平滚动卡片
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(urgentFoods, key = { it.id }) { food ->
                                UrgentFoodCard(
                                    food = food,
                                    onClick = { navController.navigate("detail/${food.id}") },
                                    onConsume = { viewModel.markConsumed(food) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. 储藏空间快捷分类
            item {
                StorageSpaceQuickGrid(
                    activeFoods = activeFoods,
                    onSpaceClick = { spaceName ->
                        viewModel.setSelectedFilter(spaceName)
                        navController.navigate("list") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 5. 库藏食品标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📦 冰箱全部库藏",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "查看全部 >",
                        fontSize = 13.sp,
                        color = FreshGreenPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            viewModel.setSelectedFilter("全部")
                            navController.navigate("list") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            // 库藏食品列表
            if (activeFoods.isEmpty()) {
                item {
                    EmptyFoodState(
                        title = "冰箱空空如也 🥬",
                        subTitle = "点击右下角「+」或选个预设食品，把美味加进冰箱吧！",
                        onAddClick = { navController.navigate("add_edit") }
                    )
                }
            } else {
                items(activeFoods, key = { it.id }) { food ->
                    FoodItemCard(
                        food = food,
                        onClick = { navController.navigate("detail/${food.id}") },
                        onConsumeClick = { viewModel.markConsumed(food) },
                        onDeleteClick = { viewModel.moveToTrash(food) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = FreshGreenPrimary
        )
    }
}
}

@Composable
fun HomeHeaderGreeting(activeCount: Int, urgentCount: Int) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 9 -> "早上好 ☀️"
        hour < 12 -> "上午好 🌿"
        hour < 14 -> "中午好 🍱"
        hour < 18 -> "下午好 🍵"
        else -> "晚上好 🌙"
    }

    val subtitle = when {
        urgentCount > 0 -> "冰箱里有 $urgentCount 件美味即将到期，今晚安排消灭吧！"
        activeCount > 0 -> "目前储藏了 $activeCount 种美味，食材都在新鲜赏味期哦～"
        else -> "开始记录你的食材，告别过期浪费！"
    }

    Column {
        Text(
            text = greeting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "鲜食记 · 智能管家",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UrgentFoodCard(
    food: FoodItem,
    onClick: () -> Unit,
    onConsume: () -> Unit
) {
    val days = food.remainingDays()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = UrgentRedBg.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 如果有图片展示图片，否则展示 Emoji
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(FreshGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    if (!food.imageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = food.imageUri,
                            contentDescription = food.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = food.iconEmoji, fontSize = 24.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(UrgentRed)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (days == 0) "今日到期 ⚠️" else "剩 ${days} 天 🔥",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = food.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )
            Text(
                text = "${food.location} · ${food.quantity}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onConsume,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("马上消灭 😋", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StorageSpaceQuickGrid(
    activeFoods: List<FoodItem>,
    onSpaceClick: (String) -> Unit
) {
    val fridgeCount = activeFoods.count { it.location.contains("冷藏") }
    val freezerCount = activeFoods.count { it.location.contains("冷冻") }
    val pantryCount = activeFoods.count { it.location.contains("常温") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SpaceCard(
            title = "冷藏室",
            emoji = "🧊",
            count = fridgeCount,
            bgColor = FridgeBlueBg,
            accentColor = FridgeBlue,
            onClick = { onSpaceClick("冷藏") },
            modifier = Modifier.weight(1f)
        )
        SpaceCard(
            title = "冷冻室",
            emoji = "❄️",
            count = freezerCount,
            bgColor = FreezerIndigoBg,
            accentColor = FreezerIndigo,
            onClick = { onSpaceClick("冷冻") },
            modifier = Modifier.weight(1f)
        )
        SpaceCard(
            title = "常温储藏",
            emoji = "🧺",
            count = pantryCount,
            bgColor = PantryWarmBg,
            accentColor = PantryWarm,
            onClick = { onSpaceClick("常温") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SpaceCard(
    title: String,
    emoji: String,
    count: Int,
    bgColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = "$count 件",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

