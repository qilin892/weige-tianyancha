package com.weige.tianyancha.ui

import androidx.compose.ui.graphics.Color

/**
 * 风鸟工商字段 → 中文标签
 *
 * 上游返回的是英文键（entName / uniscid / opScope ...），直接显示对用户毫无意义。
 * 这里做三件事：
 *  1. CORE_ORDER 决定「企业名片」区先展示哪些关键字段（其余折叠）
 *  2. LABELS 把常用键译成中文，未收录的回退原名（不丢数据）
 *  3. HIDDEN 过滤掉纯内部 ID / 空壳字段，避免界面上出现一堆无意义数字
 */
object Fields {

    /** 企业名片优先展示（顺序即展示顺序） */
    val CORE_ORDER = listOf(
        "entName", "entNameEn", "historyName", "uniscid", "regNo", "taxId",
        "personName", "entStatus", "entType", "regConcat", "recConcat",
        "esDate", "opFrom", "opTo", "apprdate", "regOrg",
        "regionName", "nicName", "dom", "yrAddress",
        "insuredCount", "tel", "email", "website",
    )

    val LABELS = mapOf(
        "entName" to "企业名称",
        "entNameEn" to "英文名",
        "historyName" to "曾用名",
        "uniscid" to "统一社会信用代码",
        "regNo" to "注册号",
        "taxId" to "纳税人识别号",
        "nacaoid" to "组织机构代码",
        "personName" to "法定代表人",
        "personEntCount" to "法人关联企业数",
        "legalPersonType" to "法人类型",
        "entStatus" to "经营状态",
        "entType" to "企业类型",
        "regConcat" to "注册资本",
        "recConcat" to "实缴资本",
        "regCap" to "注册资本(数值)",
        "regCapCn" to "注册资本(中文)",
        "esDate" to "成立日期",
        "opFrom" to "营业期限自",
        "opTo" to "营业期限至",
        "apprdate" to "核准日期",
        "regOrg" to "登记机关",
        "regionName" to "所属地区",
        "nicName" to "行业",
        "dom" to "注册地址",
        "yrAddress" to "年报地址",
        "opScope" to "经营范围",
        "entIntro" to "企业简介",
        "insuredCount" to "参保人数",
        "yrEnpNum" to "年报参保人数",
        "yrEnpNumYear" to "年报参保年份",
        "tel" to "联系电话",
        "telCount" to "电话数",
        "telList" to "全部电话",
        "email" to "邮箱",
        "emailCount" to "邮箱数",
        "emailList" to "全部邮箱",
        "website" to "官网",
        "tags" to "企业标签",
        "riskTags" to "风险标签",
        "entPic" to "企业Logo",
        "personPic" to "法人头像",
        "qynbShareHolderYear" to "年报年份",
        "companyTag" to "状态附加信息",
    )

    /** 纯内部字段，不展示 */
    val HIDDEN = setOf(
        "entid", "personId", "personType", "personTypeCode", "entTypeId", "entCategoryId",
        "regionId", "nicId", "regCapCur", "reccapOpt", "reccapOptAryear", "enttypename2",
        "isBranch", "fakesoe", "riskLevel", "socialInsuranceTrendVisible",
        "candate", "revdate", "yrAddressYear", "qynbShareHolderYear",
        "personPic", "entPic",
    )

    fun label(key: String): String = LABELS[key] ?: key

    /** 值是否值得展示（空串、纯空白、null 都跳过） */
    fun showable(key: String, value: String): Boolean {
        if (key in HIDDEN) return false
        val v = value.trim()
        if (v.isEmpty() || v == "null" || v == "{}" || v == "[]") return false
        return true
    }

    /** 排序：CORE_ORDER 内的按既定次序在前，其余保持原顺序 */
    fun ordered(pairs: List<Pair<String, String>>): List<Pair<String, String>> {
        val visible = pairs.filter { showable(it.first, it.second) }
        val core = CORE_ORDER.mapNotNull { k ->
            visible.firstOrNull { it.first == k }?.let { label(k) to it.second }
        }
        val rest = visible.filter { it.first !in CORE_ORDER }.map { label(it.first) to it.second }
        return core + rest
    }
}

/**
 * 经营状态 → 颜色
 *
 * 与后端 statusColor 保持一致：正常经营用绿（业内查企业类 App 的通行做法），
 * 迁出/清算用琥珀提示，注销/吊销用灰（已无实体，不必刺眼）。
 * 注意这里不套用「涨红跌绿」的行情约定——那是股价语义，不是企业存续语义。
 */
fun statusColor(status: String): Color = when {
    status.contains("在营") || status.contains("存续") || status.contains("开业") -> FallGreen
    status.contains("迁出") || status.contains("清算") -> Amber
    status.contains("注销") || status.contains("吊销") || status.contains("停业") -> Color(0xFF8A93A5)
    else -> AmberSoft
}
