package com.food.freshkeeper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.SortOption
import com.food.freshkeeper.ui.components.EmptyFoodState
import com.food.freshkeeper.ui.components.FoodItemCard
import com.food.freshkeeper.ui.theme.FreshGreenPrimary
import com.food.freshkeeper.ui.theme.UrgentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val foods by viewModel.filteredListFoods.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    val isBatchMode by viewModel.isBatchMode.collectAsState()
    val selectedFoodIds by viewModel.selectedFoodIds.collectAsState()
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

    var showSortMenu by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        "全部", "冷藏", "冷冻", "常温", "紧急临期", "已过期", "已消灭"
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                // 页面标题与顶部操作栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBatchMode) {
                        Text(
                            text = "已选 ${selectedFoodIds.size} 项",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { viewModel.selectAll(foods.map { it.id }) }
                            ) {
                                Text(
                                    if (selectedFoodIds.size == foods.size && foods.isNotEmpty()) "取消全选" else "全选",
                                    fontSize = 13.sp
                                )
                            }
                            TextButton(
                                onClick = { viewModel.exitBatchMode() }
                            ) {
                                Text("完成", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "食品清单 📋",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 回收站入口图标
                            IconButton(onClick = { navController.navigate("trash") }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "回收站", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // 批量管理入口
                            TextButton(
                                onClick = { viewModel.toggleBatchMode() }
                            ) {
                                Text("批量管理", fontSize = 13.sp, color = FreshGreenPrimary)
                            }

                            // 排序按钮
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.Sort, contentDescription = "排序", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = option.title,
                                                    fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (option == sortOption) FreshGreenPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSortOption(option)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("搜索食材、类别或备注...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 滚动筛选标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { option ->
                        val isSelected = selectedFilter == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedFilter(option) },
                            label = {
                                Text(
                                    text = option,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isBatchMode) {
                // 批量删除浮动按钮
                ExtendedFloatingActionButton(
                    onClick = { viewModel.deleteSelectedBatch() },
                    icon = { Icon(Icons.Default.Delete, contentDescription = "批量移入回收站") },
                    text = { Text("移入回收站 (${selectedFoodIds.size})") },
                    containerColor = if (selectedFoodIds.isNotEmpty()) UrgentRed else Color.Gray,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                )
            } else {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "找到 ${foods.size} 件物品",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isBatchMode) {
                            Text(
                                text = "点击卡片勾选以批量删除",
                                fontSize = 12.sp,
                                color = FreshGreenPrimary
                            )
                        }
                    }
                }

                if (foods.isEmpty()) {
                    item {
                        EmptyFoodState(
                            title = "没有找到符合条件的食品 🔍",
                            subTitle = "可以尝试切换筛选标签或搜索其他关键词哦",
                            onAddClick = { navController.navigate("add_edit") }
                        )
                    }
                } else {
                    items(foods, key = { it.id }) { food ->
                        FoodItemCard(
                            food = food,
                            onClick = { navController.navigate("detail/${food.id}") },
                            onConsumeClick = { viewModel.markConsumed(food) },
                            onDeleteClick = { viewModel.moveToTrash(food) },
                            isBatchMode = isBatchMode,
                            isSelected = selectedFoodIds.contains(food.id),
                            onSelectToggle = { viewModel.toggleFoodSelection(food.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
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
