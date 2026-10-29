package com.weige.tianyancha.net

import android.os.Build
import com.weige.tianyancha.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 后端 API 客户端（https://dify.eborry.com/riskbird/api）
 *
 * 无需登录：所有接口都是匿名的，服务端靠 Cookie 池与 MySQL 缓存扛住风鸟的额度限制。
 */
object Api {

    private val UA = "WeigeTianYanCha/1.0 (Android ${Build.VERSION.RELEASE})"

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)   // 冷门企业首次抓取要等上游
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun url(path: String, vararg params: Pair<String, String>): String {
        val sb = StringBuilder(BuildConfig.API_BASE).append(path)
        if (params.isNotEmpty()) {
            sb.append('?')
            params.forEachIndexed { i, (k, v) ->
                if (i > 0) sb.append('&')
                sb.append(k).append('=').append(android.net.Uri.encode(v))
            }
        }
        return sb.toString()
    }

    private fun execute(url: String, timeoutSec: Long = 120): String {
        val req = Request.Builder().url(url).header("User-Agent", UA).get().build()
        val client = if (timeoutSec == 120L) http else http.newBuilder()
            .readTimeout(timeoutSec, TimeUnit.SECONDS).build()
        val resp: Response = client.newCall(req).execute()
        val body = resp.body?.string().orEmpty()
        if (!resp.isSuccessful) {
            val msg = runCatching { JSONObject(body).optString("msg") }.getOrNull()
            throw ApiException("http_${resp.code}", msg?.ifBlank { "服务异常(${resp.code})" }
                ?: "服务异常(${resp.code})")
        }
        val root = JSONObject(body)
        if (!root.optBoolean("ok", false)) {
            throw ApiException(root.optString("err", "unknown"), root.optString("msg", "请求失败"))
        }
        // org.json 的 JSONObject.NULL 在 android.jar 里不是编译期常量，用 toString() 判断更稳
        val dataObj = root.opt("data") ?: return ""
        val text = dataObj.toString()
        return if (text == "null") "" else text
    }

    // ---------------------------------------------------------------- 搜索

    /** 搜索框自动补全（后端走 fast-search，不消耗上游额度） */
    suspend fun autocomplete(q: String, limit: Int = 10): List<Suggestion> =
        withContext(Dispatchers.IO) {
            if (q.isBlank()) return@withContext emptyList()
            val body = execute(url("/autocomplete", "q" to q, "limit" to limit.toString()), 20)
            parseArray(body).map { o ->
                Suggestion(
                    entid = o.optString("entid"),
                    name = o.optString("name"),
                    uniscid = o.optString("uniscid"),
                    status = o.optString("status"),
                    logo = o.optString("logo"),
                    segments = parseSegments(o.optJSONArray("segments")),
                )
            }
        }

    private fun parseSegments(arr: JSONArray?): List<Seg> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Seg(o.optString("t"), o.optBoolean("hl"))
        }
    }

    // ---------------------------------------------------------------- 企业

    suspend fun company(entid: String, name: String? = null): Company =
        withContext(Dispatchers.IO) {
            val params = if (name.isNullOrBlank()) emptyArray() else arrayOf("name" to name)
            val body = execute(url("/company/$entid", *params))
            val o = JSONObject(body)
            val jbxxObj = o.optJSONObject("jbxx") ?: JSONObject()
            val pairs = ArrayList<Pair<String, String>>(jbxxObj.length())
            jbxxObj.keys().forEach { k ->
                pairs.add(k to renderValue(jbxxObj.opt(k)))
            }
            val dims = ArrayList<DimItem>()
            o.optJSONArray("dimensions")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val d = arr.getJSONObject(i)
                    dims.add(
                        DimItem(
                            type = d.optString("type"),
                            name = d.optString("name"),
                            group = d.optString("group"),
                            count = d.optInt("count"),
                            cachedRows = d.optInt("cachedRows"),
                            isFull = d.optBoolean("isFull"),
                            hasCache = d.optBoolean("hasCache"),
                            fetchedAt = d.optString("fetchedAt").ifBlank { null },
                        )
                    )
                }
            }
            Company(
                entid = o.optString("entid", entid),
                name = o.optString("name"),
                uniscid = o.optString("uniscid"),
                status = o.optString("status"),
                logo = o.optString("logo"),
                jbxx = pairs,
                jbxxCount = o.optInt("jbxxCount", pairs.size),
                dimensions = dims,
                fetchedAt = o.optString("fetchedAt").ifBlank { null },
                cached = o.optBoolean("cached"),
                hitCount = o.optInt("hitCount"),
            )
        }

    suspend fun dimension(
        entid: String, dt: String, page: Int = 1, size: Int = 100, more: Boolean = false,
    ): DimensionPage = withContext(Dispatchers.IO) {
        val params = mutableListOf("page" to page.toString(), "size" to size.toString())
        if (more) params.add("more" to "1")
        val body = execute(url("/company/$entid/dimension/$dt", *params.toTypedArray()))
        val o = JSONObject(body)
        val rows = ArrayList<DimRow>()
        o.optJSONArray("rows")?.let { arr ->
            for (i in 0 until arr.length()) {
                val r = arr.optJSONObject(i) ?: continue
                val cells = ArrayList<Pair<String, String>>(r.length())
                r.keys().forEach { k -> cells.add(k to renderValue(r.opt(k))) }
                rows.add(DimRow(cells))
            }
        }
        DimensionPage(
            type = o.optString("type", dt),
            name = o.optString("name"),
            total = o.optInt("total"),
            cachedRows = o.optInt("cachedRows"),
            isFull = o.optBoolean("isFull"),
            partial = o.optBoolean("partial"),
            canLoadMore = o.optBoolean("canLoadMore"),
            page = o.optInt("page", page),
            size = o.optInt("size", size),
            rows = rows,
            fetchedAt = o.optString("fetchedAt").ifBlank { null },
        )
    }

    // ---------------------------------------------------------------- 其它

    suspend fun hot(limit: Int = 12): List<HotCompany> = withContext(Dispatchers.IO) {
        val body = execute(url("/hot", "limit" to limit.toString()), 20)
        parseArray(body).map {
            HotCompany(
                it.optString("entid"), it.optString("name"),
                it.optString("status"), it.optInt("hit_count"),
            )
        }
    }

    suspend fun stats(): Stats? = withContext(Dispatchers.IO) {
        runCatching {
            val o = JSONObject(execute(url("/stats"), 20))
            val q = o.optJSONObject("quota")
            Stats(
                companies = o.optInt("companies"),
                dimensions = o.optInt("dimensions"),
                dimensionRows = o.optInt("dimensionRows"),
                quotaRemaining = q?.optInt("remaining_today") ?: 0,
            )
        }.getOrNull()
    }

    /** 导出 Excel：返回文件字节流（大企业可能几百 KB） */
    suspend fun exportExcel(entid: String): ByteArray = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url("/company/$entid/export.xlsx"))
            .header("User-Agent", UA)
            .get().build()
        http.newBuilder().readTimeout(180, TimeUnit.SECONDS).build()
            .newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val msg = runCatching {
                        JSONObject(resp.body?.string().orEmpty()).optString("detail")
                    }.getOrNull()
                    throw ApiException("export_failed", msg?.ifBlank { null } ?: "导出失败(${resp.code})")
                }
                resp.body?.bytes() ?: ByteArray(0)
            }
    }

    // ---------------------------------------------------------------- 工具

    private fun parseArray(body: String): List<JSONObject> {
        if (body.isBlank()) return emptyList()
        val arr = runCatching { JSONArray(body) }.getOrNull() ?: return emptyList()
        return (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }
    }

    /** JSON 值 → 展示字符串：数组用「、」连接，对象转紧凑 JSON */
    private fun renderValue(v: Any?): String = when (v) {
        null, JSONObject.NULL -> ""
        is JSONArray -> (0 until v.length()).joinToString("、") { renderValue(v.opt(it)) }
        is JSONObject -> v.toString()
        is Boolean -> if (v) "是" else "否"
        is Double -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
        else -> stripHtml(v.toString())
    }

    private val TAG_RE = Regex("<[^>]{1,200}>")

    /** 上游有些字段内嵌 <span style=...> 高亮/着色标记，直接展示会露标签 */
    private fun stripHtml(s: String): String {
        if (!s.contains('<')) return s
        return TAG_RE.replace(s, "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }
}
