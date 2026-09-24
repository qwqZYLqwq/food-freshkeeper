package com.food.freshkeeper.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.food.freshkeeper.ui.theme.FreshGreenLight
import com.food.freshkeeper.ui.theme.FreshGreenPrimary
import com.food.freshkeeper.ui.theme.WarmOrangeLight

data class FoodTipItem(
    val title: String,
    val emoji: String,
    val category: String,
    val summary: String,
    val advice: String
)

val TIPS_LIST = listOf(
    FoodTipItem(
        title = "面包千万别放冷藏室！",
        emoji = "🍞",
        category = "烘焙妙招",
        summary = "冷藏室的温度(2-6℃)会极大加速淀粉的「老化回生」过程，导致面包迅速变干变硬。",
        advice = "💡 秘诀：3天内吃完放常温阴凉处；若吃不完，切片密封放入「冷冻室」，吃前烤箱或平底锅加热，即刻恢复外脆内软！"
    ),
    FoodTipItem(
        title = "鸡蛋大头朝上摆放更持久",
        emoji = "🥚",
        category = "蛋奶保鲜",
        summary = "鸡蛋的大头一端有气室，气室朝上能使蛋黄始终处于中央，不易贴在蛋壳上形成散黄。",
        advice = "💡 秘诀：买回的鸡蛋无需清洗（会破坏天然保护膜），直接大头朝上存入冰箱保鲜盒即可。"
    ),
    FoodTipItem(
        title = "草莓/浆果千万别提前洗",
        emoji = "🍓",
        category = "水果保鲜",
        summary = "草莓表皮极薄，清洗后残留的水分会迅速滋生霉菌，导致几小时内软烂发霉。",
        advice = "💡 秘诀：吃之前再清洗！保存时垫一张厨房纸吸湿，保留果蒂放入透气果蔬盒中冷藏。"
    ),
    FoodTipItem(
        title = "绿叶菜裹纸巾，锁水不烂叶",
        emoji = "🥬",
        category = "蔬菜保鲜",
        summary = "绿叶菜冷藏最怕袋内积聚水汽凝结，潮湿会导致叶片快速黄化腐烂。",
        advice = "💡 秘诀：用一张干燥厨房纸巾轻轻包裹菜根和菜叶，再装入保鲜袋冷藏，保鲜期可翻倍至5-7天！"
    ),
    FoodTipItem(
        title = "生肉海鲜冷冻分装法则",
        emoji = "🥩",
        category = "肉禽海鲜",
        summary = "大块肉反复解冻再冷冻，会导致冰晶刺破细胞膜，肉汁和鲜味大量流失，且细菌倍增。",
        advice = "💡 秘诀：买回后按单次食用分量切块分装，抽真空或紧贴保鲜膜冷冻；烹饪前提前一晚移至冷藏缓慢解冻。"
    ),
    FoodTipItem(
        title = "苹果香蕉是天然催熟剂",
        emoji = "🍎",
        category = "果蔬常识",
        summary = "苹果、香蕉会释放大量「乙烯气体」，会极大地催熟周围的西兰花、胡萝卜和绿叶菜。",
        advice = "💡 秘诀：若想保存其他蔬菜更久，务必将苹果套袋独立存放；如想催熟硬猕猴桃或牛油果，可与苹果装在同一袋里！"
    )
)

@Composable
fun TipsScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Text(
                    text = "食材保鲜小百科 💡",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "科学保存食材，锁住营养与原汁原味，告别浪费！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(TIPS_LIST) { tip ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(FreshGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tip.emoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tip.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tip.category,
                                fontSize = 11.sp,
                                color = FreshGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = tip.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = tip.advice,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
