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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.SortOption
import com.food.freshkeeper.ui.components.*
import com.food.freshkeeper.ui.theme.FreshGreenContainer
import com.food.freshkeeper.ui.theme.FreshGreenDark
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
                    .padding(top = 10.dp, bottom = 6.dp)
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
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { viewModel.selectAll(foods.map { it.id }) }
                            ) {
                                Text(
                                    if (selectedFoodIds.size == foods.size && foods.isNotEmpty()) "取消全选" else "全选",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(
                                onClick = { viewModel.exitBatchMode() }
                            ) {
                                Text("完成", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FreshGreenPrimary)
                            }
                        }
                    } else {
                        Text(
                            text = "食材清单",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 回收站入口
                            IconButton(onClick = { navController.navigate("trash") }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "回收站",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 批量管理入口
                            TextButton(
                                onClick = { viewModel.toggleBatchMode() }
                            ) {
                                Text("批量管理", fontSize = 13.sp, color = FreshGreenPrimary, fontWeight = FontWeight.Medium)
                            }

                            // 排序按钮
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(
                                        Icons.Default.Sort,
                                        contentDescription = "排序",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                // 扁平纯净搜索框
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("搜索食材、分类或备忘...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "搜索",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedBorderColor = FreshGreenPrimary,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 扁平微胶囊筛选标签
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshGreenPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (isBatchMode) {
                FreshKeeperBatchDeleteFAB(
                    count = selectedFoodIds.size,
                    onClick = { viewModel.deleteSelectedBatch() }
                )
            } else {
                FreshKeeperFAB(
                    onClick = { navController.navigate("add_edit") }
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "共 ${foods.size} 件物品",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isBatchMode) {
                            Text(
                                text = "点击卡片勾选以批量删除",
                                fontSize = 11.sp,
                                color = FreshGreenPrimary
                            )
                        }
                    }
                }

                if (foods.isEmpty()) {
                    item {
                        EmptyFoodState(
                            title = "未找到符合条件的食材",
                            subTitle = "可以尝试切换标签或清除搜索词",
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
                    Spacer(modifier = Modifier.height(115.dp))
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

