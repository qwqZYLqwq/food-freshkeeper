package com.food.freshkeeper.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val serverUrl by viewModel.serverUrl.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val lastSyncTimeMs by viewModel.lastSyncTimeMs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val defaultReminderDays by viewModel.defaultReminderDays.collectAsState()

    var inputServerUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    var showAddSampleDialog by remember { mutableStateOf(false) }
    var showClearExpiredDialog by remember { mutableStateOf(false) }
    var showClearConsumedDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (success, msg) = viewModel.sendTestExpiryNotification(context)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "通知权限被拒绝，请在系统设置中允许鲜食记发送通知", Toast.LENGTH_LONG).show()
        }
    }

    val exportZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri) { _, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri) { _, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showAddSampleDialog) {
        AlertDialog(
            onDismissRequest = { showAddSampleDialog = false },
            title = { Text("添加示范食材？") },
            text = { Text("确认添加内置示范食材？将向您的清单中注入常用的示范食材数据") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addSampleData()
                        showAddSampleDialog = false
                        Toast.makeText(context, "已成功添加示范食材 🥕", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认添加", fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSampleDialog = false }) {
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

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("从云端同步还原？") },
            text = { Text("将从云端服务器拉取食材清单并合并至本地数据库。已存在同 ID 食材将被更新，新食材将被添加。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        if (inputServerUrl != serverUrl && inputServerUrl.isNotBlank()) {
                            viewModel.setServerUrl(inputServerUrl)
                        }
                        viewModel.restoreFromCloud { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("确认拉取还原", fontWeight = FontWeight.Bold, color = FreshGreenPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(1, 3, 5, 7).forEach { days ->
                        val isSelected = defaultReminderDays == days
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setDefaultReminderDays(days)
                                Toast.makeText(context, "已设定提前 $days 天预警", Toast.LENGTH_SHORT).show()
                            },
                            label = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${days}天",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FreshGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@OutlinedButton
                            }
                        }
                        val (success, msg) = viewModel.sendTestExpiryNotification(context)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FreshGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送食物过期模拟通知 🔔", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    title = "添加示范食材",
                    subtitle = "向您的清单中注入常用的示范食材数据",
                    icon = Icons.Default.AddCircle,
                    tint = FreshGreenPrimary,
                    onClick = { showAddSampleDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                SettingActionRow(
                    title = "导出离线数据包 (ZIP)",
                    subtitle = "将所有食材数据与照片打包为单一 ZIP 归档",
                    icon = Icons.Default.Archive,
                    tint = FreshGreenPrimary,
                    onClick = {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                        exportZipLauncher.launch("鲜食记_数据备份_$timestamp.zip")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                SettingActionRow(
                    title = "导入离线数据包 (ZIP)",
                    subtitle = "从外部 ZIP 备份文件恢复食材数据与本地照片",
                    icon = Icons.Default.Unarchive,
                    tint = FreshGreenPrimary,
                    onClick = {
                        importZipLauncher.launch(
                            arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                        )
                    }
                )
            }
        }

        // 3. 云端同步与备份
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "☁️ 云端同步与备份",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = FreshGreenPrimary
                        )
                    }
                }

                Text(
                    text = "支持自定义轻量 HTTP 同步服务，离线优先设计，保障食材数据安全。",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputServerUrl,
                    onValueChange = { inputServerUrl = it },
                    label = { Text("服务器地址与端口") },
                    placeholder = { Text("例如: http://192.168.1.x:8099") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (inputServerUrl != serverUrl && inputServerUrl.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.setServerUrl(inputServerUrl)
                                    Toast.makeText(context, "服务器地址已保存 💾", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "保存地址", tint = FreshGreenPrimary)
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setServerUrl(inputServerUrl)
                            viewModel.testConnection(inputServerUrl) { success, msg ->
                                Toast.makeText(
                                    context,
                                    if (success) "连接成功: $msg ✅" else "连接失败: $msg ❌",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = !isSyncing && inputServerUrl.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("测试连接")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.setServerUrl(inputServerUrl)
                            Toast.makeText(context, "配置已保存 💾", Toast.LENGTH_SHORT).show()
                        },
                        enabled = inputServerUrl != serverUrl && inputServerUrl.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("保存配置")
                    }
                }

                HorizontalDivider()

                // 自动同步开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动同步到云端",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "增删改查食材时静默上传，离线时完全正常使用",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && inputServerUrl.isBlank() && serverUrl.isBlank()) {
                                Toast.makeText(context, "请先填写并保存服务器地址", Toast.LENGTH_SHORT).show()
                            } else {
                                if (inputServerUrl != serverUrl && inputServerUrl.isNotBlank()) {
                                    viewModel.setServerUrl(inputServerUrl)
                                }
                                viewModel.setAutoSyncEnabled(enabled)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FreshGreenPrimary
                        )
                    )
                }

                HorizontalDivider()

                // 手动同步操作按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (inputServerUrl != serverUrl && inputServerUrl.isNotBlank()) {
                                viewModel.setServerUrl(inputServerUrl)
                            }
                            viewModel.uploadBackup { _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isSyncing && (inputServerUrl.isNotBlank() || serverUrl.isNotBlank()),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("上传本地清单", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (inputServerUrl.isBlank() && serverUrl.isBlank()) {
                                Toast.makeText(context, "请先配置服务器地址", Toast.LENGTH_SHORT).show()
                            } else {
                                showRestoreDialog = true
                            }
                        },
                        enabled = !isSyncing && (inputServerUrl.isNotBlank() || serverUrl.isNotBlank()),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("从云端同步还原", fontSize = 12.sp)
                    }
                }

                // 上次同步时间显示
                val syncTimeFormatted = if (lastSyncTimeMs > 0L) {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    sdf.format(java.util.Date(lastSyncTimeMs))
                } else {
                    "尚未同步"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (lastSyncTimeMs > 0L) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (lastSyncTimeMs > 0L) FreshGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "上次同步时间: $syncTimeFormatted",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. 关于鲜食记
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
                    text = "版本 v1.3.0",
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
