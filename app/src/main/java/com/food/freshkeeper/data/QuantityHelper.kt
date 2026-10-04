package com.food.freshkeeper.data

/**
 * 食物数量与规格单位工具类
 * 支持将数量与单位拆分输入、单选下拉菜单以及离散单位的分批消灭判定
 */
object QuantityHelper {

    // 离散计数单位（支持逐个/逐包消灭）
    val DISCRETE_UNITS = listOf(
        "份", "个", "包", "袋", "盒", "罐", "瓶", "箱", "提",
        "条", "根", "块", "枚", "颗", "支", "听", "把", "片", "碗", "盘", "杯"
    )

    // 称重与容量连续单位
    val CONTINUOUS_UNITS = listOf(
        "斤", "两", "公斤", "kg", "g", "mg", "L", "ml"
    )

    // 全部预设可选单位列表
    val ALL_UNITS = DISCRETE_UNITS + CONTINUOUS_UNITS

    data class ParsedQuantity(
        val count: Int?,
        val unit: String,
        val isDiscrete: Boolean
    )

    /**
     * 解析食物数量字符串，例如 "3包", "500g", "1份", "1盒 (10枚)"
     */
    fun parse(raw: String?): ParsedQuantity {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) {
            return ParsedQuantity(1, "份", true)
        }

        // 正则提取前置整数和后续单位文本
        val regex = Regex("""^(\d+)\s*(.*)$""")
        val match = regex.find(trimmed)
        if (match != null) {
            val count = match.groupValues[1].toIntOrNull()
            var unit = match.groupValues[2].trim()
            if (unit.isEmpty()) {
                unit = "份"
            }
            // 判断单位是否在离散单位列表中（或者开头匹配某个离散单位）
            val isDiscrete = DISCRETE_UNITS.any { unit.startsWith(it) } || DISCRETE_UNITS.contains(unit)
            return ParsedQuantity(count, unit, isDiscrete)
        }

        // 如果开头不是数字，默认数量为 1，将整个文本作为单位
        val isDiscrete = DISCRETE_UNITS.any { trimmed.startsWith(it) } || DISCRETE_UNITS.contains(trimmed)
        return ParsedQuantity(1, trimmed, isDiscrete)
    }

    /**
     * 判断是否属于可选择消灭个数的多件离散食物（数量 >= 2 且为离散计数单位）
     */
    fun isMultiDiscrete(raw: String?): Boolean {
        val parsed = parse(raw)
        return parsed.isDiscrete && (parsed.count ?: 0) > 1
    }
}
