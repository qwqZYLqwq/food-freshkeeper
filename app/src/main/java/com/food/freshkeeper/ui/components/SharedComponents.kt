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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
 * 现代扁平化到期状态胶囊徽章
 */
@Composable
fun ExpiryStatusBadge(
    food: FoodItem,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val status = food.getStatus()
    val days = food.remainingDays()

    val (bg, fg, border, text) = when (status) {
        FoodStatus.CONSUMED -> if (isDark) {
            Quad(Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFF334155), "已消灭")
        } else {
            Quad(Color(0xFFF1F5F9), Color(0xFF64748B), Color(0xFFE2E8F0), "已消灭")
        }
        FoodStatus.EXPIRED -> if (isDark) {
            Quad(UrgentRed.copy(alpha = 0.16f), UrgentRed, UrgentRed.copy(alpha = 0.35f), "已过期 ${-days}天")
        } else {
            Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "已过期 ${-days}天")
        }
        FoodStatus.EXPIRING_TODAY -> if (isDark) {
            Quad(UrgentRed.copy(alpha = 0.16f), UrgentRed, UrgentRed.copy(alpha = 0.35f), "今天到期 ⚠️")
        } else {
            Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "今天到期 ⚠️")
        }
        FoodStatus.URGENT -> if (isDark) {
            Quad(UrgentRed.copy(alpha = 0.16f), UrgentRed, UrgentRed.copy(alpha = 0.35f), "还剩 ${days}天")
        } else {
            Quad(UrgentRedBg, UrgentRed, UrgentRedBorder, "还剩 ${days}天")
        }
        FoodStatus.WARNING -> if (isDark) {
            Quad(WarningAmber.copy(alpha = 0.16f), WarningAmber, WarningAmber.copy(alpha = 0.35f), "还剩 ${days}天")
        } else {
            Quad(WarningAmberBg, WarningAmber, WarningAmberBorder, "还剩 ${days}天")
        }
        FoodStatus.FRESH -> if (isDark) {
            Quad(SafeGreen.copy(alpha = 0.16f), SafeGreen, SafeGreen.copy(alpha = 0.35f), "还剩 ${days}天")
        } else {
            Quad(SafeGreenBg, SafeGreen, SafeGreenBorder, "还剩 ${days}天")
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * 扁平化储存位置微标签
 */
@Composable
fun FoodLocationTag(location: String) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val (bg, fg) = when {
        location.contains("冷藏") -> if (isDark) Pair(FridgeBlue.copy(alpha = 0.16f), Color(0xFF38BDF8)) else Pair(FridgeBlueBg, FridgeBlue)
        location.contains("冷冻") -> if (isDark) Pair(FreezerIndigo.copy(alpha = 0.16f), Color(0xFF818CF8)) else Pair(FreezerIndigoBg, FreezerIndigo)
        else -> if (isDark) Pair(PantryWarm.copy(alpha = 0.16f), Color(0xFFFBBF24)) else Pair(PantryWarmBg, PantryWarm)
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
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 极简扁平新鲜度细进度条
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
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * 现代扁平生动食品卡片
 * - 纯平极简风格，微描边无厚重投影
 * - 图标与照片圆润自适应
 * - 呼吸感操作按键，清晰明快
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

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (food.isConsumed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) FreshGreenPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = if (isBatchMode) onSelectToggle else onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 顶栏：多选勾选框、图片/Emoji、名称与位置、状态徽章
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 批量选择勾选框
                if (isBatchMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = FreshGreenPrimary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // 照片或 Emoji 现代方圆盒
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FreshGreenLight.copy(alpha = 0.6f)),
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
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 食品名称与分类
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = food.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (food.quantity.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = food.quantity,
                                fontSize = 11.sp,
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

                // 扁平状态胶囊
                ExpiryStatusBadge(food = food)
            }

            // 新鲜度进度条 (未消灭时展示)
            if (!food.isConsumed) {
                Spacer(modifier = Modifier.height(10.dp))
                FreshnessProgressBar(
                    progress = food.freshnessProgress(),
                    status = status
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 底栏信息：到期日与快捷动作
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "到期: $expiryDateStr",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 快捷操作按钮组
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 单个移入回收站
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "移入回收站",
                            tint = UrgentRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!food.isConsumed) {
                        // 扁平消灭按钮
                        FilledTonalButton(
                            onClick = onConsumeClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = FreshGreenContainer,
                                contentColor = FreshGreenDark
                            ),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("消灭", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "已享受 ✨",
                            fontSize = 11.sp,
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
 * 现代扁平首页健康概览卡片 (冰箱健康指数)
 */
@Composable
fun FridgeHealthOverviewCard(
    activeCount: Int,
    urgentCount: Int,
    expiredCount: Int,
    consumedCount: Int,
    trashCount: Int = 0,
    onActiveClick: (() -> Unit)? = null,
    onUrgentClick: () -> Unit,
    onExpiredClick: (() -> Unit)? = null,
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
        healthScore >= 90 -> "冰箱状态绝佳"
        healthScore >= 75 -> "注意及时消灭"
        else -> "有临期或过期食品"
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val scoreBg = if (isDark) {
        if (healthScore >= 80) SafeGreen.copy(alpha = 0.16f) else UrgentRed.copy(alpha = 0.16f)
    } else {
        if (healthScore >= 80) SafeGreenBg else UrgentRedBg
    }
    val scoreBorder = if (isDark) {
        if (healthScore >= 80) SafeGreen.copy(alpha = 0.35f) else UrgentRed.copy(alpha = 0.35f)
    } else {
        if (healthScore >= 80) SafeGreenBorder else UrgentRedBorder
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "冰箱新鲜指数",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = scoreStatusText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 扁平评分药丸
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(scoreBg)
                        .border(0.8.dp, scoreBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$healthScore 分",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (healthScore >= 80) SafeGreen else UrgentRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 指标胶囊块：储藏中、临期、已过期、回收站
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlatMetricPill(
                    count = activeCount,
                    label = "储藏中",
                    emoji = "🥬",
                    color = SafeGreen,
                    bgColor = SafeGreenBg,
                    onClick = onActiveClick,
                    modifier = Modifier.weight(1f)
                )
                FlatMetricPill(
                    count = urgentCount,
                    label = "临期待吃",
                    emoji = "🔥",
                    color = WarmOrange,
                    bgColor = WarmOrangeLight,
                    onClick = onUrgentClick,
                    modifier = Modifier.weight(1f)
                )
                FlatMetricPill(
                    count = expiredCount,
                    label = "已过期",
                    emoji = "⚠️",
                    color = UrgentRed,
                    bgColor = UrgentRedBg,
                    onClick = onExpiredClick,
                    modifier = Modifier.weight(1f)
                )
                FlatMetricPill(
                    count = trashCount,
                    label = "回收站",
                    emoji = "🗑️",
                    color = Color(0xFF64748B),
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = onTrashClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FlatMetricPill(
    count: Int,
    label: String,
    emoji: String,
    color: Color,
    bgColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val finalBg = if (isDark) color.copy(alpha = 0.16f) else bgColor.copy(alpha = 0.7f)
    val finalBorder = if (isDark) color.copy(alpha = 0.35f) else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(finalBg)
            .border(0.8.dp, finalBorder, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = count.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) color else color
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 现代液态玻璃悬浮底部导航栏 (1:1 深度对标 KernelSU / iOS 18 Liquid Glass 风格)
 * - 纯净双层液态高光与毛玻璃折射 (Liquid Specular Gradient & Frosted Translucency)
 * - 阻尼弹簧平滑滑动药丸指示器 (Spring Animated Lens Pill Indicator)
 * - 紧凑居中胶囊微岛，兼顾通透感与防误触
 */
@Composable
fun AppBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val tabs = remember {
        listOf(
            Triple("home", Icons.Default.Home, "首页"),
            Triple("list", Icons.AutoMirrored.Filled.FormatListBulleted, "清单"),
            Triple("tips", Icons.Default.Lightbulb, "妙招"),
            Triple("settings", Icons.Default.Settings, "设置")
        )
    }

    val selectedIndex = tabs.indexOfFirst { it.first == currentRoute }.coerceAtLeast(0)

    // 阻尼弹簧平滑滑动位置动画 (与 KernelSU 物理弹簧阻尼动画一致)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.78f,
            stiffness = 380f
        ),
        label = "liquidLensSpring"
    )

    // 液态玻璃折射渐变背景
    val glassBackgroundBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF26334D).copy(alpha = 0.72f),
                    Color(0xFF141C2B).copy(alpha = 0.88f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.90f),
                    Color(0xFFF0F4F8).copy(alpha = 0.78f)
                )
            )
        }
    }

    // 液态玻璃高光反射微边框 (上亮下柔，模拟真实玻璃顶缘反光效果)
    val glassBorderBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.40f),
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.18f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.65f)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0x3010B981),
                spotColor = if (isDark) Color.Black.copy(alpha = 0.7f) else Color(0x20000000)
            )
            .clip(CircleShape)
            .background(glassBackgroundBrush)
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        val tabWidthDp = 72.dp
        val indicatorWidthDp = 58.dp
        val indicatorHeightDp = 34.dp

        // 液态滑动药丸指示器 (Liquid Lens Glow Pill)
        Box(
            modifier = Modifier
                .offset(
                    x = tabWidthDp * animatedIndex + (tabWidthDp - indicatorWidthDp) / 2,
                    y = 3.dp
                )
                .size(width = indicatorWidthDp, height = indicatorHeightDp)
                .clip(CircleShape)
                .background(
                    if (isDark) FreshGreenPrimary.copy(alpha = 0.22f)
                    else FreshGreenPrimary.copy(alpha = 0.16f)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isDark) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.85f),
                            FreshGreenPrimary.copy(alpha = 0.25f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 导航按钮行
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, (route, icon, label) ->
                val selected = selectedIndex == index
                val activeColor = FreshGreenPrimary
                val inactiveColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .width(tabWidthDp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(route) }
                        )
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(indicatorWidthDp, indicatorHeightDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) activeColor else inactiveColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) activeColor else inactiveColor,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}

/**
 * 现代液态质感悬浮加号按钮 (Liquid Glass Floating Action Button)
 * - 翠绿液态微渐变与顶部镜面反光微边框 (Specular Bloom Border)
 * - 柔和品牌色发光投影 (Emerald Glow Shadow)
 * - 圆润触角加号图标 (Rounded Cap Add Icon)
 */
@Composable
fun FreshKeeperFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "添加食材"
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    Box(
        modifier = modifier
            .padding(bottom = 84.dp, end = 6.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x3010B981),
                spotColor = FreshGreenPrimary.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF34D399), // 顶部淡透薄荷亮绿
                        FreshGreenPrimary, // 经典翠绿
                        Color(0xFF047857)  // 底部深邃墨绿
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.70f), // 顶部玻璃镜面反光
                        Color.White.copy(alpha = 0.15f)  // 侧边与下沿微光
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .size(56.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * 现代液态质感批量操作悬浮按钮 (用于清单多选模式)
 */
@Composable
fun FreshKeeperBatchDeleteFAB(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isEnabled = count > 0

    Box(
        modifier = modifier
            .padding(bottom = 84.dp, end = 6.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x30EF4444),
                spotColor = if (isEnabled) UrgentRed.copy(alpha = 0.45f) else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isEnabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF87171),
                            UrgentRed,
                            Color(0xFFB91C1C)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF94A3B8),
                            Color(0xFF64748B)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(enabled = isEnabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "批量移入回收站",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "移入回收站 ($count)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyFoodState(
    title: String,
    subTitle: String,
    onAddClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🥗", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subTitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("记录第一件食材", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

