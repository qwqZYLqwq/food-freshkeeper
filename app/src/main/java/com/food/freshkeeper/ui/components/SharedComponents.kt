package com.food.freshkeeper.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.food.freshkeeper.data.FoodItem
import com.food.freshkeeper.data.FoodStatus
import com.food.freshkeeper.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 到期状态徽章，极其清晰醒目
 */
@Composable
fun ExpiryStatusBadge(
    food: FoodItem,
    modifier: Modifier = Modifier
) {
    val status = food.getStatus()
    val days = food.remainingDays()

    val (bg, fg, border, text) = when (status) {
        FoodStatus.CONSUMED -> Quad(Color(0xFFF1F5F9), Color(0xFF64748B), Color(0xFFCBD5E1), "已消灭 ✨")
        FoodStatus.EXPIRED -> Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "已过期 ${-days}天")
        FoodStatus.EXPIRING_TODAY -> Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "今天到期 ⚠️")
        FoodStatus.URGENT -> Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "还剩 ${days}天 🔥")
        FoodStatus.WARNING -> Quad(WarningAmberBg, WarningAmber, WarningAmberBorder, "还剩 ${days}天 ⏳")
        FoodStatus.FRESH -> Quad(SafeGreenBg, SafeGreen, SafeGreenBorder, "还剩 ${days}天 🌿")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * 储存位置小标签
 */
@Composable
fun FoodLocationTag(location: String) {
    val (bg, fg) = when {
        location.contains("冷藏") -> Pair(FridgeBlueBg, FridgeBlue)
        location.contains("冷冻") -> Pair(FreezerIndigoBg, FreezerIndigo)
        else -> Pair(PantryWarmBg, PantryWarm)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = location,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 新鲜度进度条
 */
@Composable
fun FreshnessProgressBar(
    progress: Float,
    status: FoodStatus,
    modifier: Modifier = Modifier
) {
    val barColor = when (status) {
        FoodStatus.EXPIRED -> UrgentRed
        FoodStatus.EXPIRING_TODAY, FoodStatus.URGENT -> UrgentRed
        FoodStatus.WARNING -> WarningAmber
        FoodStatus.FRESH -> SafeGreen
        FoodStatus.CONSUMED -> Color(0xFF94A3B8)
    }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "freshness")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "新鲜度",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * 生动食品卡片
 * - 支持显示真实上传的图片（若无则显示生动 Emoji）
 * - 去除了不合理的 +3天 延期按钮
 * - 增加单个删除功能（移入回收站）
 * - 批量模式下支持多选 Checkbox
 */
@Composable
fun FoodItemCard(
    food: FoodItem,
    onClick: () -> Unit,
    onConsumeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val status = food.getStatus()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val expiryDateStr = dateFormat.format(Date(food.expiryDateMs))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (food.isConsumed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (food.isConsumed) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = if (isBatchMode) onSelectToggle else onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶栏：多选框(可选)、图片/Emoji、名称、规格与状态徽章
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 批量选择勾选框
                if (isBatchMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        colors = CheckboxDefaults.colors(checkedColor = FreshGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // 照片或 Emoji 图标盒
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    FreshGreenLight.copy(alpha = 0.8f),
                                    WarmOrangeLight.copy(alpha = 0.5f)
                                )
                            )
                        ),
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
                        Text(
                            text = food.iconEmoji,
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 食品名称与分类
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (food.quantity.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = food.quantity,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FoodLocationTag(food.location)
                        Text(
                            text = food.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 状态徽章
                ExpiryStatusBadge(food = food)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 新鲜度进度条 (未消灭时展示)
            if (!food.isConsumed) {
                FreshnessProgressBar(
                    progress = food.freshnessProgress(),
                    status = status
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 底栏信息：到期日与快捷动作（消灭、删除）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "到期: $expiryDateStr",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 快捷操作按钮组：单个删除按钮 + 消灭按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 单个删除按钮 (移入回收站)
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "移入回收站",
                            tint = UrgentRed.copy(alpha = 0.85f),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (!food.isConsumed) {
                        // 消灭它按钮
                        Button(
                            onClick = onConsumeClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FreshGreenPrimary
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("消灭", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "已享受美味 😋",
                            fontSize = 12.sp,
                            color = SafeGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 首页健康概览卡片 (冰箱健康指数)
 */
@Composable
fun FridgeHealthOverviewCard(
    activeCount: Int,
    urgentCount: Int,
    expiredCount: Int,
    consumedCount: Int,
    trashCount: Int = 0,
    onUrgentClick: () -> Unit,
    onTrashClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthScore = when {
        activeCount == 0 -> 100
        expiredCount > 0 -> (100 - expiredCount * 25 - urgentCount * 10).coerceAtLeast(30)
        urgentCount > 0 -> (100 - urgentCount * 12).coerceAtLeast(60)
        else -> 98
    }

    val scoreStatusText = when {
        healthScore >= 90 -> "冰箱状态绝佳 🥗"
        healthScore >= 75 -> "注意及时消灭 🍳"
        else -> "有临期/过期食品需整理 ⚠️"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "冰箱新鲜指数",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = scoreStatusText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 评分徽章
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (healthScore >= 80) SafeGreenBg else UrgentRedBg
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$healthScore 分",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (healthScore >= 80) SafeGreen else UrgentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 指标卡：储藏中、临期、已过期、已消灭、回收站
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    count = activeCount,
                    label = "储藏中",
                    emoji = "🥬",
                    color = SafeGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    count = urgentCount,
                    label = "临期待吃",
                    emoji = "🔥",
                    color = WarmOrange,
                    onClick = onUrgentClick,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    count = expiredCount,
                    label = "已过期",
                    emoji = "⚠️",
                    color = UrgentRed,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    count = trashCount,
                    label = "回收站",
                    emoji = "🗑️",
                    color = Color(0xFF64748B),
                    onClick = onTrashClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    count: Int,
    label: String,
    emoji: String,
    color: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.1f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = count.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 底部导航栏
 */
@Composable
fun AppBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
            label = { Text("首页") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FreshGreenPrimary,
                selectedTextColor = FreshGreenPrimary,
                indicatorColor = FreshGreenLight
            )
        )
        NavigationBarItem(
            selected = currentRoute == "list",
            onClick = { onNavigate("list") },
            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "食品清单") },
            label = { Text("清单") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FreshGreenPrimary,
                selectedTextColor = FreshGreenPrimary,
                indicatorColor = FreshGreenLight
            )
        )
        NavigationBarItem(
            selected = currentRoute == "tips",
            onClick = { onNavigate("tips") },
            icon = { Icon(Icons.Default.Lightbulb, contentDescription = "保鲜妙招") },
            label = { Text("妙招") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FreshGreenPrimary,
                selectedTextColor = FreshGreenPrimary,
                indicatorColor = FreshGreenLight
            )
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
            label = { Text("设置") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FreshGreenPrimary,
                selectedTextColor = FreshGreenPrimary,
                indicatorColor = FreshGreenLight
            )
        )
    }
}

@Composable
fun EmptyFoodState(
    title: String,
    subTitle: String,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🥗 🧺 🥛", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("记录第一件食材")
            }
        }
    }
}

