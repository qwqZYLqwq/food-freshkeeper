package com.food.freshkeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

enum class FoodStatus {
    FRESH,          // 新鲜 (> 5天)
    WARNING,        // 需注意 (3..5天)
    URGENT,         // 临期紧迫 (1..2天)
    EXPIRING_TODAY, // 今天到期 (0天)
    EXPIRED,        // 已过期 (< 0天)
    CONSUMED        // 已消灭 / 已食用
}

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val category: String,          // 水果、蔬菜、肉禽、乳品、烘焙、零食饮料、熟食便当、调味干货
    val iconEmoji: String,         // 🍎, 🥦, 🥩, 🥛, 🍞, etc.
    val location: String,          // 冷藏室 🧊, 冷冻室 ❄️, 常温/阴凉 🧺
    val productionDateMs: Long,    // 生产/购入日期 (ms)
    val shelfLifeDays: Int,        // 保质期天数
    val expiryDateMs: Long,        // 到期时间戳 (ms)
    val quantity: String = "1份",  // 数量或规格，如 "500g", "2瓶"
    val notes: String = "",        // 备注备忘
    val imageUri: String? = null,  // 自定义食材照片 URI 或 本地文件路径
    val isConsumed: Boolean = false,
    val consumedAtMs: Long? = null,
    val isDeleted: Boolean = false, // 是否在回收站中
    val deletedAtMs: Long? = null,  // 移入回收站时间戳
    val reminderDaysBefore: Int = 3,
    val createdAtMs: Long = System.currentTimeMillis()
) {
    /**
     * 计算自然日剩余天数
     * 0: 今天到期
     * >0: 还有几天到期
     * <0: 已过期几天
     */
    fun remainingDays(nowMs: Long = System.currentTimeMillis()): Int {
        val todayCal = Calendar.getInstance().apply {
            timeInMillis = nowMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expiryCal = Calendar.getInstance().apply {
            timeInMillis = expiryDateMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMs = expiryCal.timeInMillis - todayCal.timeInMillis
        return (diffMs / (1000L * 60 * 60 * 24)).toInt()
    }

    /**
     * 新鲜度百分比 (0.0f ~ 1.0f)
     * 1.0f: 刚买/刚生产
     * 0.0f: 到期或已过期
     */
    fun freshnessProgress(nowMs: Long = System.currentTimeMillis()): Float {
        if (shelfLifeDays <= 0) return 0f
        val daysLeft = remainingDays(nowMs)
        if (daysLeft <= 0) return 0f
        val ratio = daysLeft.toFloat() / shelfLifeDays.toFloat()
        return ratio.coerceIn(0f, 1f)
    }

    /**
     * 当前状态
     */
    fun getStatus(nowMs: Long = System.currentTimeMillis()): FoodStatus {
        if (isConsumed) return FoodStatus.CONSUMED
        val days = remainingDays(nowMs)
        return when {
            days < 0 -> FoodStatus.EXPIRED
            days == 0 -> FoodStatus.EXPIRING_TODAY
            days <= 7 -> FoodStatus.URGENT
            else -> FoodStatus.FRESH
        }
    }

    /**
     * 是否处于紧急临期状态 (0..7天且未消灭)
     */
    fun isUrgent(nowMs: Long = System.currentTimeMillis()): Boolean {
        return !isConsumed && remainingDays(nowMs) in 0..7
    }
}
