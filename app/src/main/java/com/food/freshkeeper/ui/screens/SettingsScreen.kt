package com.food.freshkeeper.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.food.freshkeeper.R
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.ui.theme.*
import com.food.freshkeeper.util.ImageSaver
import com.food.freshkeeper.util.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: FoodViewModel
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val primaryColor = if (isDark) DarkFrostPrimary else FreshGreenPrimary
    val urgentColor = if (isDark) DarkUrgentRed else UrgentRed

    val context = LocalContext.current
    val trashFoods by viewModel.trashFoods.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsState()
    val autoSyncUploadEnabled by viewModel.autoSyncUploadEnabled.collectAsState()
    val autoSyncDownloadEnabled by viewModel.autoSyncDownloadEnabled.collectAsState()
    val lastSyncTimeMs by viewModel.lastSyncTimeMs.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val defaultReminderDays by viewModel.defaultReminderDays.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val imageSavePath by viewModel.imageSavePath.collectAsState()

    var inputServerUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    var inputImageSavePath by remember(imageSavePath) { mutableStateOf(imageSavePath) }
    var pendingAutoUpload by remember(autoSyncUploadEnabled) { mutableStateOf(autoSyncUploadEnabled) }
    var pendingAutoDownload by remember(autoSyncDownloadEnabled) { mutableStateOf(autoSyncDownloadEnabled) }

    var showAddSampleDialog by remember { mutableStateOf(false) }
    var showClearExpiredDialog by remember { mutableStateOf(false) }
    var showClearConsumedDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var updateCheckInfo by remember { mutableStateOf<UpdateManager.UpdateCheckInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val (_, msg) = viewModel.sendTestExpiryNotification(context)
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
            title = { Text("添加示范食材？", fontWeight = FontWeight.Bold) },
            text = { Text("确认添加内置示范食材？将向您的清单中注入常用的示范食材数据") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addSampleData()
                        showAddSampleDialog = false
                        Toast.makeText(context, "已成功添加示范食材 🥕", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("确认添加", fontWeight = FontWeight.Bold, color = primaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSampleDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showClearExpiredDialog) {
        AlertDialog(
            onDismissRequest = { showClearExpiredDialog = false },
            title = { Text("清理所有已过期食材？", fontWeight = FontWeight.Bold) },
            text = { Text("将批量将当前已过期未食用的食材移入回收站。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearExpired()
                        showClearExpiredDialog = false
                        Toast.makeText(context, "已过期食材已移入回收站 🧹", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("移入回收站", color = urgentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearExpiredDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showClearConsumedDialog) {
        AlertDialog(
            onDismissRequest = { showClearConsumedDialog = false },
            title = { Text("清空已消灭历史？", fontWeight = FontWeight.Bold) },
            text = { Text("确定清空所有标记为已食用的历史记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConsumed()
                        showClearConsumedDialog = false
                        Toast.makeText(context, "历史记录已移入回收站", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("清空", color = urgentColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConsumedDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("从云端同步还原？", fontWeight = FontWeight.Bold) },
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
                    Text("确认拉取还原", fontWeight = FontWeight.Bold, color = primaryColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showUpdateDialog && updateCheckInfo != null) {
        val info = updateCheckInfo!!
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(
                    text = if (info.isLatest) "已是最新版本 🎉" else "发现新版本 ${info.latestVersion} 🚀",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (info.isLatest) {
                            "当前使用的鲜食记 (v${info.currentVersion}) 已经是最新版，暂无更新。\n(云端最新发布: ${info.latestVersion})"
                        } else {
                            "检测到新版本发布！\n当前版本: v${info.currentVersion}\n最新版本: ${info.latestVersion}"
                        },
                        fontSize = 13.sp
                    )
                    if (!info.isLatest && info.releaseNotes.isNotBlank()) {
                        Text(
                            text = "更新说明：\n${info.releaseNotes.take(160)}...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "⚡ 下载将自动采用 GitHub 国内高速加速通道，并在系统通知栏显示实时下载进度。",
                        fontSize = 11.sp,
                        color = primaryColor
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        UpdateManager.startAcceleratedDownload(
                            context = context,
                            acceleratedUrl = info.acceleratedDownloadUrl,
                            releaseVersion = info.latestVersion
                        )
                    }
                ) {
                    Text(
                        text = if (info.isLatest) "重新下载安装包 (加速通道)" else "立即加速下载",
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text(
                        text = if (info.isLatest) "知道了" else "稍后再说",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "系统与设置",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 1. 临期与消息通知偏好
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "临期与过期提醒",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "在食材临近到期或已过期时向系统发送状态栏通知",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            viewModel.setNotificationEnabled(isChecked)
                            Toast.makeText(context, if (isChecked) "已开启消息通知提醒 🔔" else "已关闭消息通知提醒 🔕", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "临期预警阈值天数",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "系统将在食材到期前向您高亮预警，避免遗忘导致变质",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(3, 7, 14, 30).forEach { days ->
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
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = if (isDark) DarkBackground else Color.White,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        if (!notificationEnabled) {
                            viewModel.setNotificationEnabled(true)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@OutlinedButton
                            }
                        }
                        val (_, msg) = viewModel.sendTestExpiryNotification(context)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("发送食物过期模拟通知", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. 回收站与数据管理
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "回收站与数据管理",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                SettingActionRow(
                    title = "食材回收站",
                    subtitle = "当前有 ${trashFoods.size} 件已删除食材，可恢复或彻底清空",
                    icon = Icons.Outlined.Delete,
                    tint = primaryColor,
                    onClick = { navController.navigate("trash") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                SettingActionRow(
                    title = "清理所有已过期食材",
                    subtitle = "一键将已过期未食用的物品移入回收站",
                    icon = Icons.Default.DeleteSweep,
                    tint = urgentColor,
                    onClick = { showClearExpiredDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                SettingActionRow(
                    title = "清空已消灭历史记录",
                    subtitle = "将已食用的食材历史档案移入回收站",
                    icon = Icons.Default.History,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { showClearConsumedDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                SettingActionRow(
                    title = "添加示范食材",
                    subtitle = "向您的清单中注入常用的示范食材数据",
                    icon = Icons.Default.AddCircle,
                    tint = primaryColor,
                    onClick = { showAddSampleDialog = true }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                SettingActionRow(
                    title = "导出离线数据包 (ZIP)",
                    subtitle = "将所有食材数据与照片打包为单一 ZIP 归档",
                    icon = Icons.Default.Archive,
                    tint = primaryColor,
                    onClick = {
                        val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                        exportZipLauncher.launch("鲜食记_数据备份_$timestamp.zip")
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                SettingActionRow(
                    title = "导入离线数据包 (ZIP)",
                    subtitle = "从外部 ZIP 备份文件恢复食材数据与本地照片",
                    icon = Icons.Default.Unarchive,
                    tint = primaryColor,
                    onClick = {
                        importZipLauncher.launch(
                            arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream", "*/*")
                        )
                    }
                )
            }
        }

        // 3. 图片保存目录
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "图片保存目录",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "全屏查看食材照片时点击下载，将保存至系统公共下载区指定的子目录中。",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputImageSavePath,
                    onValueChange = { inputImageSavePath = it },
                    label = { Text("保存目标目录", fontSize = 13.sp) },
                    placeholder = { Text("默认: Download/food") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (inputImageSavePath.isNotBlank() && inputImageSavePath != imageSavePath) {
                            IconButton(onClick = {
                                val sanitized = ImageSaver.sanitizeImageSavePath(inputImageSavePath)
                                inputImageSavePath = sanitized
                                viewModel.updateImageSavePath(sanitized)
                                Toast.makeText(context, "已保存目录设置: $sanitized", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "应用",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // 快捷预设标签组
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "快捷预设路径:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf("Download/food", "Download/鲜食记", "Download/食材档案")
                        presets.forEach { preset ->
                            val isSelected = imageSavePath == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    inputImageSavePath = preset
                                    viewModel.updateImageSavePath(preset)
                                    Toast.makeText(context, "已切换保存目录: $preset", Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(text = preset, fontSize = 11.sp)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = if (isDark) DarkBackground else Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.resetImageSavePath()
                            inputImageSavePath = ImageSaver.DEFAULT_IMAGE_SAVE_PATH
                            Toast.makeText(context, "已重置为默认保存目录: ${ImageSaver.DEFAULT_IMAGE_SAVE_PATH}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("重置默认", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val sanitized = ImageSaver.sanitizeImageSavePath(inputImageSavePath)
                            inputImageSavePath = sanitized
                            viewModel.updateImageSavePath(sanitized)
                            Toast.makeText(context, "已更新保存目录: $sanitized", Toast.LENGTH_SHORT).show()
                        },
                        enabled = inputImageSavePath.isNotBlank() && inputImageSavePath != imageSavePath,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("保存修改", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. 云端同步与备份
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "云端同步与备份",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = primaryColor
                        )
                    }
                }

                Text(
                    text = "支持自定义轻量 HTTP 同步服务，离线优先设计，保障食材数据安全。",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = inputServerUrl,
                    onValueChange = { inputServerUrl = it },
                    label = { Text("服务器地址与端口", fontSize = 13.sp) },
                    placeholder = { Text("例如: http://192.168.1.x:8099") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (inputServerUrl != serverUrl && inputServerUrl.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.setServerUrl(inputServerUrl)
                                    Toast.makeText(context, "服务器地址已保存 💾", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "保存地址", tint = primaryColor, modifier = Modifier.size(18.dp))
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("测试连接", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.setServerUrl(inputServerUrl)
                            Toast.makeText(context, "配置已保存 💾", Toast.LENGTH_SHORT).show()
                        },
                        enabled = inputServerUrl != serverUrl && inputServerUrl.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存配置", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                // 自动同步总开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "开启自动同步",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "后台智能处理食材变动同步，离线优先设计",
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
                            checkedTrackColor = primaryColor
                        )
                    )
                }

                // 自动同步细分选项与确认生效
                AnimatedVisibility(visible = autoSyncEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "自动同步选项（二者至少选择一个）：",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // 选项 1: 是否自动上传
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val next = !pendingAutoUpload
                                    if (!next && !pendingAutoDownload) {
                                        Toast.makeText(context, "自动上传与自动下载二者至少选择一个", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAutoUpload = next
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = pendingAutoUpload,
                                onCheckedChange = { checked ->
                                    if (!checked && !pendingAutoDownload) {
                                        Toast.makeText(context, "自动上传与自动下载二者至少选择一个", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAutoUpload = checked
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1. 是否自动上传",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "本地食材变动时自动上传至云端（自动同步不同步回收站）",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 选项 2: 是否自动下载
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val next = !pendingAutoDownload
                                    if (!next && !pendingAutoUpload) {
                                        Toast.makeText(context, "自动上传与自动下载二者至少选择一个", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAutoDownload = next
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = pendingAutoDownload,
                                onCheckedChange = { checked ->
                                    if (!checked && !pendingAutoUpload) {
                                        Toast.makeText(context, "自动上传与自动下载二者至少选择一个", Toast.LENGTH_SHORT).show()
                                    } else {
                                        pendingAutoDownload = checked
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "2. 是否自动下载",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "下拉刷新或启动应用时自动拉取云端新食材并显示新增提示",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val isValid = pendingAutoUpload || pendingAutoDownload
                        val hasChanges = pendingAutoUpload != autoSyncUploadEnabled || pendingAutoDownload != autoSyncDownloadEnabled

                        Button(
                            onClick = {
                                if (!isValid) {
                                    Toast.makeText(context, "自动上传与自动下载二者至少选择一个！", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.setAutoSyncOptions(upload = pendingAutoUpload, download = pendingAutoDownload)
                                    val uploadDesc = if (pendingAutoUpload) "自动上传[开]" else "自动上传[关]"
                                    val downloadDesc = if (pendingAutoDownload) "自动下载[开]" else "自动下载[关]"
                                    Toast.makeText(context, "自动同步配置已确认生效：$uploadDesc, $downloadDesc ✅", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isValid,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasChanges) "确认生效 (待应用)" else "确认生效",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("从云端还原", fontSize = 12.sp)
                    }
                }

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
                        tint = if (lastSyncTimeMs > 0L) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "上次同步时间: $syncTimeFormatted",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. 关于鲜食记
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) DarkFrostPrimaryContainer else FreshGreenLight.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🥗", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "鲜食记 · FoodFresh",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "版本 v1.9.0 · 现代扁平设计",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "随身鲜味管家 · 不浪费舌尖上的美好 🌿",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 6. 作者个人信息卡片 (参考 1.jpg 样式)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    UpdateManager.openInBrowser(context, UpdateManager.GITHUB_PROFILE_URL)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 圆形头像 (使用 GitHub 头像，内置离线资源兜底)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isDark) DarkSurfaceVariant else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://avatars.githubusercontent.com/u/110767171?v=4")
                            .placeholder(R.drawable.author_github_avatar)
                            .error(R.drawable.author_github_avatar)
                            .fallback(R.drawable.author_github_avatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "作者 GitHub 头像",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "awaZYLawa",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "@qwqZYLqwq",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 7. 开源主页与检查更新 (已移除贡献者、翻译、支持)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                AuthorOptionItem(
                    title = "官方网站",
                    subtitle = "访问 GitHub 项目开源主页",
                    onClick = { UpdateManager.openInBrowser(context, UpdateManager.GITHUB_REPO_URL) }
                )

                // 检查与下载更新 (先检测版本，再走高速加速通道)
                AuthorOptionItem(
                    title = "检查与下载更新",
                    subtitle = if (isDownloadingUpdate) "正在检测云端版本..." else "当前版本 v1.9.0 · 先比对版本，后走加速路线",
                    showProgress = isDownloadingUpdate,
                    primaryColor = primaryColor,
                    onClick = {
                        if (isDownloadingUpdate) {
                            Toast.makeText(context, "正在检测或下载中，请稍候...", Toast.LENGTH_SHORT).show()
                            return@AuthorOptionItem
                        }
                        isDownloadingUpdate = true
                        Toast.makeText(context, "正在检测 GitHub 最新发布版本...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            val result = UpdateManager.checkVersion("1.9.0")
                            isDownloadingUpdate = false
                            val checkInfo = result.getOrNull()
                            if (checkInfo != null) {
                                updateCheckInfo = checkInfo
                                showUpdateDialog = true
                            } else {
                                Toast.makeText(context, "检测版本失败，请检查网络或点击官方网站查看", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
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
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AuthorOptionItem(
    title: String,
    subtitle: String? = null,
    showProgress: Boolean = false,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = primaryColor
            )
        } else {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

