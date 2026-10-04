package com.food.freshkeeper.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.food.freshkeeper.FoodViewModel
import com.food.freshkeeper.data.FoodItem
import com.food.freshkeeper.ui.components.ExpiryStatusBadge
import com.food.freshkeeper.ui.components.FoodLocationTag
import com.food.freshkeeper.ui.components.FreshnessProgressBar
import com.food.freshkeeper.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavHostController,
    viewModel: FoodViewModel,
    foodId: Long
) {
    val context = LocalContext.current
    val food by viewModel.getFoodById(foodId).collectAsState(initial = null)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf(false) }
    var isSavingImage by remember { mutableStateOf(false) }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val currentFood = food
            val imageUri = currentFood?.imageUri
            if (!imageUri.isNullOrBlank()) {
                isSavingImage = true
                viewModel.saveFoodImageToPublicDownload(
                    context = context,
                    imageUri = imageUri,
                    foodName = currentFood.name
                ) { success, resultMessage ->
                    isSavingImage = false
                    if (success) {
                        Toast.makeText(context, "图片已保存至: $resultMessage", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "保存失败: $resultMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "保存失败：需要存储读写权限才能保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    val saveFoodImageAction: () -> Unit = {
        val currentFood = food
        val imageUri = currentFood?.imageUri
        if (imageUri.isNullOrBlank()) {
            Toast.makeText(context, "保存失败：当前食材无实物图片", Toast.LENGTH_SHORT).show()
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                    isSavingImage = true
                    viewModel.saveFoodImageToPublicDownload(
                        context = context,
                        imageUri = imageUri,
                        foodName = currentFood.name
                    ) { success, resultMessage ->
                        isSavingImage = false
                        if (success) {
                            Toast.makeText(context, "图片已保存至: $resultMessage", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "保存失败: $resultMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    writePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                isSavingImage = true
                viewModel.saveFoodImageToPublicDownload(
                    context = context,
                    imageUri = imageUri,
                    foodName = currentFood.name
                ) { success, resultMessage ->
                    isSavingImage = false
                    if (success) {
                        Toast.makeText(context, "图片已保存至: $resultMessage", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "保存失败: $resultMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isExpanded = true
    }
    val animProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "containerReflow"
    )

    if (showDeleteConfirm && food != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("移入回收站？", fontWeight = FontWeight.Bold) },
            text = { Text("「${food!!.name}」将被移入回收站，随时可恢复，不影响当前新鲜度统计。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.moveToTrash(food!!)
                        showDeleteConfirm = false
                        navController.popBackStack()
                    }
                ) {
                    Text("移入回收站", color = UrgentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食材详情", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("add_edit?foodId=$foodId") }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = FreshGreenPrimary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = UrgentRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (food == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FreshGreenPrimary)
            }
        } else {
            val item = food!!
            val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
            val prodDateStr = dateFormat.format(Date(item.productionDateMs))
            val expDateStr = dateFormat.format(Date(item.expiryDateMs))
            val days = item.remainingDays()

            val imageSize = (54f + (100f - 54f) * animProgress).dp
            val imageCorner = (14f + (22f - 14f) * animProgress).dp
            val emojiSize = (28f + (50f - 28f) * animProgress).sp
            val heroPadding = (14f + (20f - 14f) * animProgress).dp
            val contentAlpha = animProgress.coerceIn(0f, 1f)
            val contentOffsetY = ((1f - animProgress) * 20).dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. 顶部食材主角卡片
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(heroPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(imageSize)
                                .clip(RoundedCornerShape(imageCorner))
                                .background(FreshGreenLight.copy(alpha = 0.6f))
                                .then(
                                    if (!item.imageUri.isNullOrBlank()) {
                                        Modifier.clickable { showImagePreview = true }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.imageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.imageUri,
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ZoomIn,
                                        contentDescription = "查看大图",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else {
                                Text(text = item.iconEmoji, fontSize = emojiSize)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = item.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FoodLocationTag(item.location)
                            Text(
                                text = "·  ${item.category}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (item.quantity.isNotBlank()) {
                                Text(
                                    text = "·  ${item.quantity}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ExpiryStatusBadge(food = item)
                    }
                }

                // 2. 新鲜度与保质时间信息卡
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetY.toPx()
                        }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "保质状态总览",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FreshnessProgressBar(
                            progress = item.freshnessProgress(),
                            status = item.getStatus()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailInfoRow(label = "生产/购买日期", value = prodDateStr, icon = Icons.Default.CalendarMonth)
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailInfoRow(label = "保质期时长", value = "${item.shelfLifeDays} 天", icon = Icons.Default.HourglassBottom)
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailInfoRow(
                            label = "到期截止日",
                            value = expDateStr,
                            icon = Icons.Default.EventBusy,
                            valueColor = if (days <= 2) UrgentRed else FreshGreenDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        DetailInfoRow(
                            label = "赏味倒计时",
                            value = when {
                                item.isConsumed -> "已消灭 ✨"
                                days < 0 -> "已过期 ${-days} 天"
                                days == 0 -> "今天截止！"
                                else -> "还剩 $days 天"
                            },
                            icon = Icons.Default.Timer,
                            valueColor = if (days <= 2) UrgentRed else FreshGreenDark
                        )
                    }
                }

                // 3. 备忘灵感卡
                if (item.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = contentAlpha
                                translationY = contentOffsetY.toPx()
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "备忘与保存笔记", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.notes,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                // 4. 底部快捷操作卡
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = contentAlpha
                            translationY = contentOffsetY.toPx()
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "快捷操作", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (!item.isConsumed) {
                            Button(
                                onClick = {
                                    viewModel.requestConsume(item, onSingleConsumed = {
                                        navController.popBackStack()
                                    })
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("消灭它！标记已食用", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { showDeleteConfirm = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgentRed),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("移入回收站", fontSize = 13.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.markActive(item) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("恢复为储藏中 ↩️", fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showImagePreview && food?.imageUri != null) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 4f)
            if (scale > 1f) {
                offset += panChange
            } else {
                offset = Offset.Zero
            }
        }

        Dialog(
            onDismissRequest = {
                scale = 1f
                offset = Offset.Zero
                showImagePreview = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
                    .clickable {
                        scale = 1f
                        offset = Offset.Zero
                        showImagePreview = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = food!!.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "实物档案照片 (可捏合或双击放大)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                showImagePreview = false
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "关闭预览")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = food!!.imageUri,
                            contentDescription = food!!.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                                .transformable(state = transformableState)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (scale > 1.2f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            } else {
                                                scale = 2.5f
                                            }
                                        }
                                    )
                                }
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }

                    Text(
                        text = "双击切换放大 · 点击空白关闭",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (!isSavingImage) {
                            saveFoodImageAction()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 24.dp)
                        .size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = FreshGreenPrimary,
                        contentColor = Color.White
                    )
                ) {
                    if (isSavingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "保存图片到本地",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

