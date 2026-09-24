package com.food.freshkeeper.data

import java.util.Calendar

data class FoodCategory(
    val name: String,
    val iconEmoji: String,
    val defaultShelfLifeDays: Int,
    val defaultLocation: String
)

data class FoodPreset(
    val name: String,
    val category: String,
    val iconEmoji: String,
    val location: String,
    val defaultShelfLifeDays: Int,
    val defaultQuantity: String,
    val storageTip: String
)

object FoodDataPresets {

    val LOCATIONS = listOf(
        "冷藏室 🧊",
        "冷冻室 ❄️",
        "常温储藏 🧺"
    )

    val CATEGORIES = listOf(
        FoodCategory("全部", "🌈", 7, "冷藏室 🧊"),
        FoodCategory("水果", "🍎", 7, "冷藏室 🧊"),
        FoodCategory("蔬菜", "🥦", 5, "冷藏室 🧊"),
        FoodCategory("肉禽海鲜", "🥩", 60, "冷冻室 ❄️"),
        FoodCategory("乳品蛋类", "🥛", 14, "冷藏室 🧊"),
        FoodCategory("烘焙面点", "🍞", 4, "常温储藏 🧺"),
        FoodCategory("零食饮品", "🧃", 90, "常温储藏 🧺"),
        FoodCategory("熟食便当", "🍱", 2, "冷藏室 🧊"),
        FoodCategory("调味副食", "🥫", 180, "常温储藏 🧺")
    )

    val PRESET_ITEMS = listOf(
        FoodPreset("鲜牛奶", "乳品蛋类", "🥛", "冷藏室 🧊", 7, "1盒 (950ml)", "开启后请于48小时内饮用完毕，避免冰箱门频繁温差"),
        FoodPreset("鲜草莓", "水果", "🍓", "冷藏室 🧊", 3, "1盒 (300g)", "吃之前再清洗，带蒂冷藏透气保鲜盒中可延长保鲜"),
        FoodPreset("红富士苹果", "水果", "🍎", "常温储藏 🧺", 14, "4颗", "苹果会释放乙烯催熟其他蔬果，建议套袋独立存放"),
        FoodPreset("西兰花", "蔬菜", "🥦", "冷藏室 🧊", 5, "1颗", "避免与苹果、香蕉同放，纸巾包裹吸收水分最佳"),
        FoodPreset("生鲜牛排", "肉禽海鲜", "🥩", "冷冻室 ❄️", 90, "2片 (300g)", "分装密封抽真空冷冻，烹饪前提前一晚移至冷藏室缓慢解冻"),
        FoodPreset("无菌鲜鸡蛋", "乳品蛋类", "🥚", "冷藏室 🧊", 30, "1盒 (10枚)", "大头朝上放置，蛋壳气室呼吸不易散黄"),
        FoodPreset("全麦吐司", "烘焙面点", "🍞", "常温储藏 🧺", 4, "1袋 (6片)", "若吃不完切勿冷藏(会加速淀粉老化)，可切片冷冻保存"),
        FoodPreset("北极甜虾仁", "肉禽海鲜", "🦐", "冷冻室 ❄️", 60, "1袋 (400g)", "冷冻锁鲜，解冻后不宜二次冷冻"),
        FoodPreset("自制红烧牛肉", "熟食便当", "🍱", "冷藏室 🧊", 3, "1保鲜盒", "出锅放凉后立即密封入冰箱，食用前需彻底热透"),
        FoodPreset("鲜橙汁饮料", "零食饮品", "🧃", "冷藏室 🧊", 10, "1瓶 (1L)", "低温冷藏风味更佳，开盖后尽快饮用"),
        FoodPreset("日式生巧蛋糕", "烘焙面点", "🍰", "冷藏室 🧊", 2, "1块", "含有动物奶油与鲜果，建议尽早消灭"),
        FoodPreset("嫩豆腐", "蔬菜", "🧊", "冷藏室 🧊", 3, "1盒", "开封后可浸入淡盐凉开水中冷藏，每日换水"),
        FoodPreset("纯甄酸奶", "乳品蛋类", "🍶", "冷藏室 🧊", 21, "1联 (4杯)", "活性乳酸菌需2-6℃冷藏"),
        FoodPreset("三文鱼刺身", "肉禽海鲜", "🍣", "冷藏室 🧊", 1, "200g", "生食水产建议当天尽早食用完毕"),
        FoodPreset("番茄", "蔬菜", "🍅", "常温储藏 🧺", 7, "5个", "常温避光保存风味更浓郁，熟透后可移入冷藏")
    )

    /**
     * 生成初始默认示范数据，让首次进入生动丰富
     */
    fun createInitialMockFoods(): List<FoodItem> {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        return listOf(
            // 紧急临期 1天：生巧蛋糕
            FoodItem(
                name = "日式生巧蛋糕",
                category = "烘焙面点",
                iconEmoji = "🍰",
                location = "冷藏室 🧊",
                productionDateMs = now - oneDay,
                shelfLifeDays = 2,
                expiryDateMs = now + oneDay,
                quantity = "1盒 (2块)",
                notes = "昨天下午买的，含有生巧奶油，今天下午茶一定要消灭它！😋",
                reminderDaysBefore = 1
            ),
            // 临期 2天：鲜草莓
            FoodItem(
                name = "鲜草莓",
                category = "水果",
                iconEmoji = "🍓",
                location = "冷藏室 🧊",
                productionDateMs = now - oneDay,
                shelfLifeDays = 3,
                expiryDateMs = now + 2 * oneDay,
                quantity = "1盒 (300g)",
                notes = "丹东红颜草莓，香甜多汁，洗净直接吃超赞",
                reminderDaysBefore = 2
            ),
            // 临期 3天：鲜牛奶
            FoodItem(
                name = "鲜牛奶",
                category = "乳品蛋类",
                iconEmoji = "🥛",
                location = "冷藏室 🧊",
                productionDateMs = now - 4 * oneDay,
                shelfLifeDays = 7,
                expiryDateMs = now + 3 * oneDay,
                quantity = "1盒 (950ml)",
                notes = "巴氏杀菌鲜奶，早餐配燕麦",
                reminderDaysBefore = 3
            ),
            // 状态良好 5天：西兰花
            FoodItem(
                name = "西兰花",
                category = "蔬菜",
                iconEmoji = "🥦",
                location = "冷藏室 🧊",
                productionDateMs = now - oneDay,
                shelfLifeDays = 6,
                expiryDateMs = now + 5 * oneDay,
                quantity = "1颗",
                notes = "周末清炒虾仁或水煮减脂餐",
                reminderDaysBefore = 2
            ),
            // 充足状态 20天：无菌鲜鸡蛋
            FoodItem(
                name = "无菌鲜鸡蛋",
                category = "乳品蛋类",
                iconEmoji = "🥚",
                location = "冷藏室 🧊",
                productionDateMs = now - 5 * oneDay,
                shelfLifeDays = 25,
                expiryDateMs = now + 20 * oneDay,
                quantity = "1盒 (10枚)",
                notes = "大头朝上冷藏，可做溏心蛋",
                reminderDaysBefore = 3
            ),
            // 冷冻长期 70天：雪花牛排
            FoodItem(
                name = "生鲜雪花牛排",
                category = "肉禽海鲜",
                iconEmoji = "🥩",
                location = "冷冻室 ❄️",
                productionDateMs = now - 20 * oneDay,
                shelfLifeDays = 90,
                expiryDateMs = now + 70 * oneDay,
                quantity = "2片 (300g)",
                notes = "真空独立包装，家庭聚餐大餐备用",
                reminderDaysBefore = 7
            ),
            // 已消灭的食品（成就感展示）：全麦吐司
            FoodItem(
                name = "全麦吐司",
                category = "烘焙面点",
                iconEmoji = "🍞",
                location = "常温储藏 🧺",
                productionDateMs = now - 6 * oneDay,
                shelfLifeDays = 4,
                expiryDateMs = now - 2 * oneDay,
                quantity = "1袋 (6片)",
                notes = "美味做成三明治吃光啦！",
                isConsumed = true,
                consumedAtMs = now - oneDay,
                reminderDaysBefore = 1
            )
        )
    }
}
