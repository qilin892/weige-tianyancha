package com.weige.tianyancha.net

/** 搜索高亮分段：后端已把 HTML 拆好，App 直接按段渲染 */
data class Seg(val t: String, val hl: Boolean)

data class Suggestion(
    val entid: String,
    val name: String,
    val uniscid: String,
    val status: String,
    val logo: String,
    val segments: List<Seg>,
)

data class DimItem(
    val type: String,
    val name: String,
    val group: String,
    val count: Int,
    val cachedRows: Int,
    val isFull: Boolean,
    val hasCache: Boolean,
    val fetchedAt: String?,
)

data class Company(
    val entid: String,
    val name: String,
    val uniscid: String,
    val status: String,
    val logo: String,
    val jbxx: List<Pair<String, String>>,   // 有序键值对（保留上游字段顺序）
    val jbxxCount: Int,
    val dimensions: List<DimItem>,
    val fetchedAt: String?,
    val cached: Boolean,
    val hitCount: Int,
) {
    /** 维度按后端给的顺序分组（后端已按 工商/基本/法律/风险/经营/发展/知识产权 排好） */
    val groups: List<Pair<String, List<DimItem>>>
        get() {
            val ordered = LinkedHashMap<String, MutableList<DimItem>>()
            dimensions.forEach { d -> ordered.getOrPut(d.group) { mutableListOf() }.add(d) }
            return ordered.map { it.key to it.value.toList() }
        }

    val totalDimRows: Int get() = dimensions.sumOf { it.cachedRows }
}

/** 一条维度记录：保持字段顺序，便于表格按列展示 */
data class DimRow(val cells: List<Pair<String, String>>)

data class DimensionPage(
    val type: String,
    val name: String,
    val total: Int,
    val cachedRows: Int,
    val isFull: Boolean,
    val partial: Boolean,
    val canLoadMore: Boolean,
    val page: Int,
    val size: Int,
    val rows: List<DimRow>,
    val fetchedAt: String?,
)

data class HotCompany(
    val entid: String,
    val name: String,
    val status: String,
    val logo: String,
    val hitCount: Int,
)

data class Stats(
    val companies: Int,
    val dimensions: Int,
    val dimensionRows: Int,
    val quotaRemaining: Int,
)

/** 后端统一错误：err 是机器码，message 是可直接展示的中文 */
class ApiException(val code: String, override val message: String) : Exception(message)
