package com.weige.tianyancha.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 风鸟字段 → 中文标签 / 图标 / 取值语义
 *
 * 三件套都放这里，保证「工商信息」和「维度记录」两处口径完全一致：
 *  1. LABELS + 自动分词翻译 → 界面上不再出现英文键名
 *  2. ICONS  → 每个字段配一个语义图标
 *  3. valueKind() → 判断该怎么渲染（电话可拨、邮箱可发、网址可开、状态用标签…）
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
        // ---------------- 工商基本信息 ----------------
        "entName" to "企业名称",
        "entname" to "企业名称",
        "entNameEn" to "英文名",
        "historyName" to "曾用名",
        "uniscid" to "统一社会信用代码",
        "uniscId" to "统一社会信用代码",
        "regNo" to "注册号",
        "regno" to "注册号",
        "taxId" to "纳税人识别号",
        "taxNo" to "纳税人识别号",
        "nacaoid" to "组织机构代码",
        "personName" to "法定代表人",
        "faren" to "法定代表人",
        "personEntCount" to "法人关联企业数",
        "legalPersonType" to "法人类型",
        "entStatus" to "经营状态",
        "busSt" to "经营状态",
        "entType" to "企业类型",
        "entTypeCode" to "企业类型",
        "regConcat" to "注册资本",
        "recConcat" to "实缴资本",
        "regcap" to "注册资本",
        "regCap" to "注册资本",
        "regCapCn" to "注册资本",
        "esDate" to "成立日期",
        "gdate" to "成立日期",
        "opFrom" to "营业期限自",
        "opTo" to "营业期限至",
        "apprdate" to "核准日期",
        "regOrg" to "登记机关",
        "regionName" to "所属地区",
        "region" to "所属地区",
        "province" to "所属省份",
        "provinceCn" to "所属省份",
        "countryCn" to "国家/地区",
        "nicName" to "行业",
        "dom" to "注册地址",
        "yrAddress" to "年报地址",
        "opScope" to "经营范围",
        "entIntro" to "企业简介",
        "intro" to "简介",
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
        "postalCode" to "邮政编码",
        "tags" to "企业标签",
        "riskTags" to "风险标签",
        "empNum" to "员工人数",
        "empNumText" to "员工人数",
        "womEmpNum" to "女性员工数",
        "mainBusiAct" to "主营活动",
        "orgNum" to "机构数量",
        "regName" to "登记名称",
        "subjectType" to "主体类型",
        "name" to "名称",
        "title" to "标题",
        "content" to "内容",
        "value" to "值",
        "version" to "版本",
        "to" to "至",
        "dateYear" to "年份",
        "annoYear" to "年份",

        // ---------------- 股东 / 人员 / 控制关系 ----------------
        "shareHolderName" to "股东名称",
        "shareholderName" to "股东名称",
        "shaName" to "股东名称",
        "shareHolderType" to "股东类型",
        "shareholderTypeText" to "股东类型",
        "shaType" to "股东类型",
        "parIns" to "投资人",
        "parInsCode" to "投资人代码",
        "subConAm" to "认缴金额",
        "subConAmFormat" to "认缴金额",
        "subConAmOri" to "认缴金额(原币)",
        "subConDate" to "认缴日期",
        "subConForm" to "认缴形式",
        "subConRatio" to "认缴比例",
        "realConAm" to "实缴金额",
        "realConAmOri" to "实缴金额(原币)",
        "realConDate" to "实缴日期",
        "fundedRatio" to "出资比例",
        "ratio" to "比例",
        "voteRatio" to "表决权比例",
        "shareHolderRelateCount" to "关联企业数",
        "position" to "职位",
        "roleType" to "角色",
        "education" to "学历",
        "controllerName" to "实际控制人",
        "controllerEntCount" to "控制企业数",
        "controlledCompanyCount" to "控股企业数",
        "acProp" to "持股比例",
        "beneficiaryName" to "受益所有人",
        "beneficiaryRatio" to "受益比例",
        "beneficiaryTypeDesc" to "受益类型",
        "beneficiaryEntCount" to "受益企业数",
        "benefitName" to "受益股东",
        "benefitRatio" to "受益比例",
        "benefitKind" to "受益方式",
        "benefitEntCount" to "受益企业数",
        "concertedAction" to "一致行动人",
        "isOwner" to "是否本人",

        // ---------------- 变更 / 年报 ----------------
        "altItem" to "变更事项",
        "altItemCodeCn" to "变更类型",
        "altBe" to "变更前",
        "altAf" to "变更后",
        "altDate" to "变更日期",
        "decdate" to "决定日期",
        "decideReason" to "决定原因",
        "ancheYear" to "年报年度",
        "arCheYear" to "年报年度",
        "arCheDate" to "年报日期",
        "arCheDateText" to "年报日期",
        "yearReportShareHolderList" to "年报股东",
        "evalYear" to "评价年度",
        "creditRating" to "信用等级",

        // ---------------- 对外投资 / 分支 ----------------
        "entCount" to "企业数",
        "investNum" to "投资笔数",
        "investRatio" to "投资比例",
        "invType" to "投资类型",
        "conAm" to "投资金额",
        "conAmOri" to "投资金额(原币)",
        "conDate" to "投资日期",
        "conForm" to "投资形式",
        "amount" to "金额",
        "buyInDate" to "出资日期",
        "widthDrawlDate" to "退出日期",
        "hasEquityText" to "股权出质",
        "depth" to "层级",

        // ---------------- 招聘 / 招投标 / 客户 ----------------
        "salary" to "薪资",
        "experience" to "经验要求",
        "jobRequirement" to "岗位要求",
        "jobResponsibility" to "岗位职责",
        "releaseDate" to "发布日期",
        "coopTimes" to "合作次数",
        "tenderee" to "招标方",
        "bidder" to "中标方",
        "bidderList" to "中标方",
        "biderType" to "中标方类型",
        "tendereeType" to "招标方类型",
        "totalamount" to "中标金额",
        "totalamountText" to "中标金额",
        "totalamountName" to "中标金额",
        "awardDate" to "中标日期",

        // ---------------- 证书 / 资质 / 许可 ----------------
        "certNum" to "证书编号",
        "certificateCode" to "证书编号",
        "certProject" to "认证项目",
        "certStatus" to "证书状态",
        "qualificationType" to "资质类型",
        "agency" to "代理机构",
        "agencyType" to "代理机构类型",
        "agencyList" to "代理机构",
        "pubNum" to "公告号",
        "note" to "备注",
        "noteContent" to "备注",
        "account" to "账户",
        "bank" to "银行",
        "taxAuthority" to "主管税务机关",
        "taxOrg" to "税务机关",
        "evalOrg" to "评价机构",
        "awardOrg" to "颁发机构",
        "awardContent" to "奖项内容",
        "reportYear" to "报告年度",
        "isbusi" to "是否营业",
        "operatorName" to "经营者名称",
        "liccode" to "许可证编号",
        "licname" to "许可名称",
        "liccontent" to "许可内容",
        "licanth" to "许可机关",
        "lictype" to "许可类型",
        "licenceState" to "许可状态",
        "valform" to "有效期自",
        "valto" to "有效期至",

        // ---------------- 标准 ----------------
        "standardNo" to "标准号",
        "standardCode" to "标准号",
        "standardName" to "标准名称",
        "standardType" to "标准类型",
        "standardStatus" to "标准状态",
        "publishDate" to "发布日期",
        "pdate" to "发布日期",
        "implementDate" to "实施日期",

        // ---------------- 司法 ----------------
        "caseNo" to "案号",
        "caseReason" to "案由",
        "caseType" to "案件类型",
        "caseAmount" to "涉案金额",
        "cause" to "案由",
        "court" to "法院",
        "judgeDate" to "裁判日期",
        "plaintiff" to "原告",
        "defendant" to "被告",
        "docType" to "文书类型",
        "trialProcedure" to "审理程序",

        // ---------------- 经营风险 ----------------
        "addReason" to "列入原因",
        "addDate" to "列入日期",
        "removeReason" to "移出原因",
        "removeDate" to "移出日期",
        "decisionOrg" to "决定机关",
        "punishNo" to "处罚决定书文号",
        "punishType" to "处罚类型",
        "punishContent" to "处罚内容",
        "punishAmount" to "罚款金额",
        "punishOrg" to "处罚机关",
        "punishDate" to "处罚日期",
        "illegalFact" to "违法事实",
        "executeCourt" to "执行法院",
        "executeTarget" to "执行标的",
        "executeAmount" to "执行金额",
        "lienceStatus" to "履行情况",
        "performance" to "履行情况",
        "specificCircumstances" to "失信情形",
        "filingDate" to "立案日期",
        "caseStatus" to "案件状态",
        "checkOrg" to "检查机关",
        "checkDate" to "检查日期",
        "checkResult" to "检查结果",
        "checkType" to "检查类型",

        // ---------------- 知识产权 ----------------
        "patentName" to "专利名称",
        "patentType" to "专利类型",
        "patentNum" to "专利号",
        "patentStatus" to "专利状态",
        "applicationDate" to "申请日期",
        "applicant" to "申请人",
        "inventor" to "发明人",
        "ipcClass" to "分类号",
        "tmName" to "商标名称",
        "tmNo" to "商标号",
        "intCls" to "国际分类",
        "applyDate" to "申请日期",
        "regDate" to "登记日期",
        "tmStatus" to "商标状态",
        "softwareName" to "软件名称",
        "softwareNo" to "登记号",
        "owner" to "著作权人",
        "category" to "类别",
        "workName" to "作品名称",
        "wname" to "网站名称",
        "wno" to "备案号",
        "domain" to "域名",
        "icpNo" to "ICP备案号",
        "auditDate" to "审核日期",
        "websiteName" to "网站名称",

        // ---------------- 股权 / 担保 ----------------
        "pledgor" to "出质人",
        "pledgee" to "质权人",
        "equityAmount" to "出质股权数额",
        "mortgagee" to "抵押权人",
        "mortgagor" to "抵押人",
        "regCode" to "登记编号",
        "status" to "状态",
    )

    /** 纯内部字段：id、图片、HTML、树结构、平台附加标记，展示了只会干扰阅读 */
    val HIDDEN = setOf(
        "entid", "entId", "id", "idStr", "mid", "nodeType", "parentId", "depth",
        "entTypeId", "entCategoryId", "regionId", "nicId", "regCapCur",
        "reccapOpt", "reccapOptAryear", "enttypename2",
        "isBranch", "fakesoe", "riskLevel", "socialInsuranceTrendVisible",
        "candate", "revdate", "yrAddressYear", "qynbShareHolderYear",
        "entPic", "entpic", "pic", "personPic", "personType", "personTypeCode",
        "shaId", "shaPic", "shareHolderId", "shareHolderPic", "shareholderId",
        "beneficiaryId", "beneficiaryPic", "beneficiaryDataId", "benefitId",
        "benefitPic", "benefitType", "benefitTypeCode", "beneficiaryType",
        "beneficiaryTypeCode", "controllerId", "controllerPic", "controllerQueryId",
        "controllerTypeCode", "relatId", "sourceId", "agencyIdStr", "biderIdStr",
        "tendereeIdStr", "shaTypeCode", "shareHolderTypeCode", "shareholderType",
        "pathList", "pathSize", "detailPage", "pdfUrl", "contentHtml",
        "list", "entStatusRiskLevel", "entoLogo",
        "holdingSmsg", "holdingSmsgDis", "holdingSmsgDisText",
        "hasExtguaranteeText", "hasInvestText", "hasWebsiteText", "hasPersonProfile",
        "entPic", "logo",
        // 变更事项的机器码（12/70 这种数字），中文名在 altItemCodeCn 里
        "altItemCode",
        // 平台给企业打的状态附加标记，不属于用户要查的工商信息
        "companyTag", "tags", "riskTags", "statusTags", "entStatusTags",
        "dataSource", "source", "subjectType", "sourceType", "dataFrom",
        // 内部计数与重复表达（金额等已有正式字段）
        "telCount", "emailCount", "regCap", "regCapCn",
    )

    // ---------------------------------------------------------------- 图标

    private val ICONS: Map<String, ImageVector> = buildMap {
        listOf("entName", "name", "entname", "title").forEach { put(it, Icons.Filled.Business) }
        listOf("personName", "faren", "shareHolderName", "shareholderName", "shaName",
            "controllerName", "beneficiaryName", "benefitName", "inventor", "owner",
            "plaintiff", "defendant", "applicant").forEach { put(it, Icons.Filled.Person) }
        listOf("regConcat", "recConcat", "regcap", "regCap", "regCapCn", "subConAm",
            "subConAmFormat", "realConAm", "conAm", "amount", "caseAmount", "punishAmount",
            "executeAmount", "totalamount", "totalamountText", "equityAmount",
            "salary", "investNum").forEach { put(it, Icons.Filled.Payments) }
        listOf("esDate", "gdate", "apprdate", "opFrom", "opTo", "altDate", "arCheDate",
            "judgeDate", "publishDate", "pdate", "releaseDate", "applyDate", "regDate",
            "subConDate", "realConDate", "conDate", "punishDate", "addDate", "removeDate",
            "filingDate", "checkDate", "awardDate", "implementDate", "auditDate",
            "decdate", "buyInDate", "widthDrawlDate").forEach { put(it, Icons.Filled.Event) }
        listOf("dom", "yrAddress", "regionName", "region", "province", "provinceCn",
            "countryCn", "court", "executeCourt", "regOrg", "checkOrg", "punishOrg",
            "decisionOrg", "licanth", "taxOrg", "taxAuthority").forEach { put(it, Icons.Filled.Place) }
        listOf("tel", "telList").forEach { put(it, Icons.Filled.Phone) }
        listOf("email", "emailList").forEach { put(it, Icons.Filled.Email) }
        listOf("website", "wname", "domain", "icpNo", "websiteName").forEach { put(it, Icons.Filled.Language) }
        listOf("uniscid", "uniscId", "regNo", "regno", "taxId", "taxNo", "nacaoid",
            "regCode", "certNum", "certificateCode", "caseNo", "punishNo", "standardNo",
            "liccode", "softwareNo", "tmNo", "patentNum").forEach { put(it, Icons.Filled.Badge) }
        listOf("entType", "category", "standardType", "docType", "caseType", "invType",
            "biderType", "tendereeType", "qualificationType", "altItemCodeCn",
            "altItem", "type").forEach { put(it, Icons.Filled.Category) }
        listOf("opScope", "entIntro", "intro", "content", "jobRequirement",
            "jobResponsibility", "illegalFact", "specificCircumstances",
            "liccontent", "punishContent", "note").forEach { put(it, Icons.Filled.Description) }
        listOf("insuredCount", "empNum", "empNumText", "womEmpNum", "personEntCount",
            "entCount", "orgNum", "checkResult").forEach { put(it, Icons.Filled.Groups) }
        listOf("position", "roleType", "education", "experience", "mainBusiAct",
            "nicName", "licenseState", "licenceState", "busSt", "entStatus").forEach {
            put(it, Icons.Filled.Work)
        }
        listOf("patentName", "tmName", "softwareName", "workName", "certProject",
            "awardContent", "evalYear", "creditRating").forEach { put(it, Icons.Filled.Lightbulb) }
        listOf("addReason", "removeReason", "punishType", "decideReason", "riskTags")
            .forEach { put(it, Icons.Filled.Warning) }
        listOf("court", "trialProcedure", "caseReason", "cause").forEach { put(it, Icons.Filled.Gavel) }
        listOf("validFrom", "valform", "valto", "opFrom", "opTo", "expireDate",
            "implementDate", "releaseDate", "dateYear", "ancheYear", "arCheYear")
            .forEach { put(it, Icons.Filled.Schedule) }
        listOf("tags", "status", "caseStatus", "tmStatus", "patentStatus",
            "certStatus", "lienceStatus", "performance").forEach { put(it, Icons.Filled.Tag) }
    }

    fun iconOf(key: String): ImageVector? =
        ICONS[key] ?: ICONS[if (key.endsWith("Clean")) key.dropLast(5) else key]

    /** 维度（按中文名）配图标，让列表一眼能分出是"人/钱/诉讼/知识产权" */
    fun dimensionIcon(name: String): ImageVector = when {
        name.contains("股东") || name.contains("人员") || name.contains("职员") ||
            name.contains("控制人") || name.contains("受益") || name.contains("法人") ->
            Icons.Filled.Person

        name.contains("招聘") || name.contains("岗位") -> Icons.Filled.Work

        name.contains("诉讼") || name.contains("案件") || name.contains("裁判") ||
            name.contains("法院") || name.contains("公告") || name.contains("送达") ||
            name.contains("调解") -> Icons.Filled.Gavel

        name.contains("专利") || name.contains("商标") || name.contains("著作权") ||
            name.contains("标准") || name.contains("资质") || name.contains("证书") ||
            name.contains("备案") || name.contains("作品") || name.contains("App") ||
            name.contains("小程序") || name.contains("软著") -> Icons.Filled.Lightbulb

        name.contains("投资") || name.contains("分支") || name.contains("集团") ||
            name.contains("客户") || name.contains("供应商") || name.contains("招标") ||
            name.contains("中标") || name.contains("竞品") || name.contains("项目") ->
            Icons.Filled.Business

        name.contains("风险") || name.contains("异常") || name.contains("处罚") ||
            name.contains("失信") || name.contains("被执行") || name.contains("抽查") ||
            name.contains("召回") || name.contains("抵押") || name.contains("出质") ->
            Icons.Filled.Warning

        name.contains("税") || name.contains("海关") || name.contains("信用") ||
            name.contains("债券") || name.contains("上市") -> Icons.Filled.Badge

        name.contains("域名") || name.contains("网站") || name.contains("ICP") ->
            Icons.Filled.Language

        name.contains("土地") || name.contains("地址") || name.contains("地区") ->
            Icons.Filled.Place

        name.contains("变更") || name.contains("年报") || name.contains("经营") ||
            name.contains("资本") || name.contains("股东") -> Icons.Filled.Description

        else -> Icons.Filled.Description
    }

    // ---------------------------------------------------------------- 标签

    /** 未收录字段的兜底翻译：按驼峰/下划线切词，逐词译，译不出就保留原文 */
    private val TOKENS = mapOf(
        "ent" to "企业", "company" to "企业", "name" to "名称", "title" to "标题",
        "person" to "人员", "share" to "股份", "shareholder" to "股东", "holder" to "持有人",
        "controller" to "控制人", "beneficiary" to "受益人", "benefit" to "受益",
        "date" to "日期", "time" to "时间", "year" to "年份", "day" to "日",
        "amount" to "金额", "money" to "金额", "capital" to "资本", "cap" to "资本",
        "num" to "数量", "count" to "数量", "number" to "编号", "no" to "编号",
        "code" to "代码", "type" to "类型", "status" to "状态", "state" to "状态",
        "reason" to "原因", "content" to "内容", "desc" to "说明", "description" to "说明",
        "address" to "地址", "addr" to "地址", "tel" to "电话", "phone" to "电话",
        "email" to "邮箱", "mail" to "邮箱", "url" to "链接", "link" to "链接",
        "org" to "机构", "organ" to "机构", "agency" to "机构", "court" to "法院",
        "case" to "案件", "law" to "法律", "legal" to "法律", "risk" to "风险",
        "patent" to "专利", "trademark" to "商标", "tm" to "商标", "copyright" to "著作权",
        "cert" to "证书", "certificate" to "证书", "license" to "许可", "licence" to "许可",
        "industry" to "行业", "business" to "经营", "scope" to "范围", "ratio" to "比例",
        "rate" to "比例", "percent" to "比例", "level" to "等级", "grade" to "等级",
        "source" to "来源", "remark" to "备注", "note" to "备注", "version" to "版本",
        "list" to "列表", "info" to "信息", "detail" to "详情", "result" to "结果",
        "before" to "变更前", "after" to "变更后", "change" to "变更", "item" to "项目",
        "belong" to "所属", "main" to "主要", "sub" to "子", "total" to "总",
        "province" to "省份", "city" to "城市", "region" to "地区", "area" to "区域",
        "start" to "开始", "end" to "结束", "from" to "自", "to" to "至",
        "create" to "创建", "update" to "更新", "apply" to "申请", "publish" to "发布",
        "valid" to "有效", "expire" to "到期", "reg" to "登记", "check" to "检查",
        "punish" to "处罚", "execute" to "执行", "bid" to "中标", "tender" to "招标",
        "is" to "", "has" to "有", "flag" to "标记",
    )

    private fun autoLabel(key: String): String? {
        // camelCase / snake_case → 词列表
        val parts = key.replace('_', ' ')
            .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
            .replace(Regex("([A-Z]+)([A-Z][a-z])"), "$1 $2")
            .lowercase()
            .split(' ')
            .filter { it.isNotBlank() }
        if (parts.isEmpty()) return null
        val translated = parts.map { TOKENS[it] ?: return null }
        val text = translated.joinToString("").trim()
        return text.ifBlank { null }
    }

    fun label(key: String): String {
        LABELS[key]?.let { return it }
        val base = if (key.endsWith("Clean")) key.dropLast(5) else key
        LABELS[base]?.let { return it }
        return autoLabel(base) ?: base
    }

    /** 值是否值得展示（空值、纯空白、null、JSON 空壳都跳过） */
    fun showable(key: String, value: String): Boolean {
        if (key in HIDDEN) return false
        val v = value.trim()
        if (v.isEmpty() || v == "null" || v == "{}" || v == "[]" || v == "-") return false
        return true
    }

    /** 一条记录里真正要展示的字段：有 `xxxClean` 时只留 Clean（带单位/差值，信息更全） */
    fun visibleCells(cells: List<Pair<String, String>>): List<Pair<String, String>> {
        val present = cells.filter { showable(it.first, it.second) }
        val names = present.mapTo(HashSet()) { it.first }
        return present.filter { (k, _) -> "${k}Clean" !in names }
    }

    /** 排序：CORE_ORDER 内的按既定次序在前，其余保持原顺序（返回原始键，渲染时再翻译） */
    fun ordered(pairs: List<Pair<String, String>>): List<Pair<String, String>> {
        val visible = pairs.filter { showable(it.first, it.second) }
        val core = CORE_ORDER.mapNotNull { k -> visible.firstOrNull { it.first == k } }
        val rest = visible.filter { it.first !in CORE_ORDER }
        return core + rest
    }
}

/** 取值语义：决定用哪种方式渲染 */
enum class ValueKind {
    Status,      // 经营状态/案件状态 → 彩色标签
    Phone,       // 可点击拨号
    Email,       // 可点击发信
    Url,         // 可点击打开
    Money,       // 金额/资本 → 加粗强调
    Number,      // 纯数字
    Date,        // 日期
    Bool,        // 是/否
    Long,        // 长文本 → 折叠展开
    Text,
}

private val PHONE_KEYS = setOf("tel", "telList", "phone", "mobile", "contactTel")
private val EMAIL_KEYS = setOf("email", "emailList", "mail")
private val URL_KEYS = setOf("website", "domain", "url", "icpNo", "wname", "websiteName")
private val STATUS_KEYS = setOf(
    "entStatus", "busSt", "status", "caseStatus", "tmStatus", "patentStatus",
    "certStatus", "lienceStatus", "performance", "licenceState", "creditRating",
)
private val MONEY_KEYS = setOf(
    "regConcat", "recConcat", "regcap", "regCap", "regCapCn", "subConAm", "subConAmFormat",
    "realConAm", "conAm", "amount", "caseAmount", "punishAmount", "executeAmount",
    "totalamount", "totalamountText", "equityAmount", "salary",
)

private val DATE_RE = Regex("""^\d{4}[-/年]\d{1,2}([-/月]\d{1,2}日?)?.*$""")
private val PHONE_RE = Regex("""^[\d\-+() ]{7,20}$""")
private val EMAIL_RE = Regex("""^[\w.+-]+@[\w-]+\.[\w.-]+$""")

fun valueKind(key: String, value: String): ValueKind {
    val k = if (key.endsWith("Clean")) key.dropLast(5) else key
    val v = value.trim()
    return when {
        k in STATUS_KEYS -> ValueKind.Status
        // 电话必须「字段名像电话」才算，否则注册号、日期这类纯数字会被误判
        k in PHONE_KEYS || (k.contains("tel") && PHONE_RE.matches(v)) -> ValueKind.Phone
        k in EMAIL_KEYS || EMAIL_RE.matches(v) -> ValueKind.Email
        k in URL_KEYS || v.startsWith("http") -> ValueKind.Url
        v == "是" || v == "否" || v.equals("true", true) || v.equals("false", true) -> ValueKind.Bool
        k in MONEY_KEYS -> ValueKind.Money
        DATE_RE.matches(v) -> ValueKind.Date
        v.length > 48 -> ValueKind.Long
        v.toDoubleOrNull() != null -> ValueKind.Number
        else -> ValueKind.Text
    }
}

/** 经营状态 → 颜色 */
fun statusColor(status: String): Color = when {
    status.contains("在营") || status.contains("存续") || status.contains("开业") ||
        status.contains("有效") || status.contains("正常") -> FallGreen
    status.contains("迁出") || status.contains("清算") -> Amber
    status.contains("注销") || status.contains("吊销") || status.contains("停业") ||
        status.contains("无效") || status.contains("失信") -> Color(0xFF8A93A5)
    else -> AmberSoft
}
