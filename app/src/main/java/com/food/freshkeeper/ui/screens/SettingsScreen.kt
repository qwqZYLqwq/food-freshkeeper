package com.food.freshkeeper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.ui.theme.FreshGreenLight
import com.food.freshkeeper.ui.theme.FreshGreenPrimary
import com.food.freshkeeper.ui.theme.UrgentRed

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val context = LocalContext.current
    val trashFoods by viewModel.trashFoods.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearExpiredDialog by remember { mutableStateOf(false) }
    var showClearConsumedDialog by remember { mutableStateOf(false) }

    var defaultReminderDays by remember { mutableIntStateOf(3) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重置并加载示范食材？") },
            text = { Text("这将清空当前食材并恢复初始丰富多样的示范食材，方便立即体验各项功能。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSampleData()
                        showResetDialog = false
                        Toast.makeText(context, "示范食材已加载完成！✨", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认重置", fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearExpiredDialog) {
        AlertDialog(
            onDismissRequest = { showClearExpiredDialog = false },
            title = { Text("清理所有已过期食材？") },
            text = { Text("将批量将当前已过期未食用的食材移入回收站。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearExpired()
                        showClearExpiredDialog = false
                        Toast.makeText(context, "已过期食材已移入回收站 🧹", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("移入回收站", color = UrgentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearExpiredDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearConsumedDialog) {
        AlertDialog(
            onDismissRequest = { showClearConsumedDialog = false },
            title = { Text("清空已消灭历史？") },
            text = { Text("确定清空所有标记为已食用的历史记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConsumed()
                        showClearConsumedDialog = false
                        Toast.makeText(context, "历史记录已移入回收站", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("清空", color = UrgentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConsumedDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "系统与偏好设置 ⚙️",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // 1. 临期提醒偏好
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🔔 临期预警阈值",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "系统将在食材到期前向您高亮预警，避免遗忘导致变质。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { days ->
                        val isSelected = defaultReminderDays == days
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                defaultReminderDays = days
                                Toast.makeText(context, "已设定提前 $days 天预警", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("提前${days}天") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 2. 回收站与数据管理 (Requirement 5)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "🧹 回收站与数据管理",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 回收站入口
                SettingActionRow(
                    title = "食材回收站",
                    subtitle = "当前有 ${trashFoods.size} 件已删除食材，可恢复或彻底清空",
                    icon = Icons.Outlined.Delete,
                    tint = FreshGreenPrimary,
                    onClick = { navController.navigate("trash") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                SettingActionRow(
                    title = "清理所有已过期食材",
                    subtitle = "一键移出已过期未能食用的物品至回收站",
                    icon = Icons.Default.DeleteSweep,
                    tint = UrgentRed,
                    onClick = { showClearExpiredDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                SettingActionRow(
                    title = "清空已消灭历史记录",
                    subtitle = "将已食用的食材历史档案移入回收站",
                    icon = Icons.Default.History,
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = { showClearConsumedDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                SettingActionRow(
                    title = "恢复丰富示范食材",
                    subtitle = "清空并重新填入草莓、牛排、牛奶等示范数据",
                    icon = Icons.Default.Refresh,
                    tint = FreshGreenPrimary,
                    onClick = { showResetDialog = true }
                )
            }
        }

        // 3. 关于鲜食记
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(FreshGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🥗", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "鲜食记 · FoodFresh",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "版本 v1.1.0",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "陪伴你的随身鲜味管家\n不浪费每一口舌尖上的美好 🌿",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
