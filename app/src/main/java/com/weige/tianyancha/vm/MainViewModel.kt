package com.weige.tianyancha.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weige.tianyancha.net.Api
import com.weige.tianyancha.net.ApiException
import com.weige.tianyancha.net.Company
import com.weige.tianyancha.net.DimensionPage
import com.weige.tianyancha.net.DimItem
import com.weige.tianyancha.net.HotCompany
import com.weige.tianyancha.net.Stats
import com.weige.tianyancha.net.Suggestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Search : Screen
    data class Company(val entid: String, val name: String) : Screen
    data class Dimension(val entid: String, val dt: String, val name: String) : Screen
}

class MainViewModel : ViewModel() {

    // ---------------- 导航 ----------------
    var screen by mutableStateOf<Screen>(Screen.Search)
        private set

    // ---------------- 搜索 ----------------
    var query by mutableStateOf("")
        private set
    var suggestions by mutableStateOf<List<Suggestion>>(emptyList())
        private set
    var suggesting by mutableStateOf(false)
        private set
    var showSuggestions by mutableStateOf(false)
        private set

    // ---------------- 企业 ----------------
    var company by mutableStateOf<Company?>(null)
        private set
    var companyLoading by mutableStateOf(false)
        private set
    var companyError by mutableStateOf<String?>(null)
        private set

    // ---------------- 维度 ----------------
    var dimPage by mutableStateOf<DimensionPage?>(null)
        private set
    var dimLoading by mutableStateOf(false)
        private set
    var dimLoadingMore by mutableStateOf(false)
        private set
    var dimError by mutableStateOf<String?>(null)
        private set

    // ---------------- 其它 ----------------
    var hot by mutableStateOf<List<HotCompany>>(emptyList())
        private set
    var stats by mutableStateOf<Stats?>(null)
        private set
    var toast by mutableStateOf<String?>(null)

    private var suggestJob: Job? = null

    init {
        viewModelScope.launch {
            runCatching { Api.hot(12) }.onSuccess { hot = it }
            stats = Api.stats()
        }
    }

    fun consumeToast() { toast = null }

    // ---------------------------------------------------------------- 搜索

    fun onQueryChange(q: String) {
        query = q
        suggestJob?.cancel()
        if (q.isBlank()) {
            suggestions = emptyList()
            showSuggestions = false
            suggesting = false
            return
        }
        showSuggestions = true
        suggesting = true
        suggestJob = viewModelScope.launch {
            delay(260)   // 防抖：连续输入只打最后一次
            try {
                suggestions = Api.autocomplete(q, 10)
            } catch (e: Exception) {
                suggestions = emptyList()
            } finally {
                suggesting = false
            }
        }
    }

    fun clearQuery() {
        suggestJob?.cancel()
        query = ""
        suggestions = emptyList()
        showSuggestions = false
    }

    fun hideSuggestions() { showSuggestions = false }

    /** 键盘上的「搜索」：直接取补全前 20 条 */
    fun submitSearch() {
        val q = query.trim()
        if (q.isEmpty()) return
        suggestJob?.cancel()
        showSuggestions = true
        suggesting = true
        viewModelScope.launch {
            try {
                suggestions = Api.autocomplete(q, 20)
                if (suggestions.isEmpty()) toast = "没有找到相关企业"
            } catch (e: Exception) {
                toast = (e as? ApiException)?.message ?: "搜索失败，请检查网络"
            } finally {
                suggesting = false
            }
        }
    }

    // ---------------------------------------------------------------- 企业详情

    fun openCompany(entid: String, name: String) {
        showSuggestions = false
        if (entid.isBlank()) {
            toast = "该企业缺少唯一标识，请换一个关键词"
            return
        }
        screen = Screen.Company(entid, name)
        loadCompany(entid, name)
    }

    fun loadCompany(entid: String, name: String, silent: Boolean = false) {
        if (companyLoading) return
        companyLoading = true
        companyError = null
        viewModelScope.launch {
            try {
                company = Api.company(entid, name.ifBlank { null })
                if (!silent) {
                    val c = company
                    if (c != null) toast = if (c.cached) "已从本地库读取" else "已抓取最新数据"
                }
            } catch (e: Exception) {
                companyError = (e as? ApiException)?.message ?: "加载失败：${e.message}"
            } finally {
                companyLoading = false
            }
        }
    }

    fun refreshCompany() {
        val s = screen
        if (s is Screen.Company) {
            company = null
            loadCompany(s.entid, s.name)
        }
    }

    // ---------------------------------------------------------------- 维度

    fun openDimension(item: DimItem) {
        val s = screen as? Screen.Company ?: return
        screen = Screen.Dimension(s.entid, item.type, item.name)
        dimPage = null
        dimError = null
        loadDimension(s.entid, item.type, more = false)
    }

    fun loadDimension(entid: String, dt: String, more: Boolean) {
        if (more) {
            if (dimLoadingMore) return
            dimLoadingMore = true
        } else {
            if (dimLoading) return
            dimLoading = true
            dimError = null
        }
        viewModelScope.launch {
            try {
                val before = dimPage?.cachedRows ?: 0
                val page = Api.dimension(entid, dt, page = 1, size = 200, more = more)
                dimPage = page
                if (more && page.cachedRows <= before) {
                    toast = "上游暂时限流，稍后再试"
                }
            } catch (e: Exception) {
                val msg = (e as? ApiException)?.message ?: "加载失败：${e.message}"
                if (more) toast = msg else dimError = msg
            } finally {
                dimLoading = false
                dimLoadingMore = false
            }
        }
    }

    fun loadMore() {
        val s = screen as? Screen.Dimension ?: return
        loadDimension(s.entid, s.dt, more = true)
    }

    // ---------------------------------------------------------------- 返回

    /** 返回上一级；返回 true 表示已处理（调用方不必再退出 App） */
    fun back(): Boolean = when (val s = screen) {
        is Screen.Dimension -> {
            screen = Screen.Company(s.entid, company?.name ?: "")
            true
        }
        is Screen.Company -> {
            screen = Screen.Search
            company = null
            true
        }
        Screen.Search -> false
    }
}
