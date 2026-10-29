package com.weige.tianyancha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weige.tianyancha.net.DimRow
import com.weige.tianyancha.net.DimensionPage
import com.weige.tianyancha.vm.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionScreen(vm: MainViewModel, title: String, onBack: () -> Unit) {
    val page = vm.dimPage

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        if (page != null) {
                            Text(
                                "共 ${formatCount(page.total)} 条 · 本地已缓存 ${page.cachedRows}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when {
                vm.dimLoading && page == null -> LoadingBlock("正在读取…")
                vm.dimError != null -> EmptyBlock("读取失败", vm.dimError)
                page == null || page.rows.isEmpty() -> EmptyBlock(
                    "暂无数据",
                    "该维度在缓存里没有记录，可在详情页下拉刷新后重试",
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    itemsIndexed(page.rows) { idx, row ->
                        RowCard(idx + 1, row)
                    }
                    if (page.canLoadMore) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    "当前显示 ${page.cachedRows} / ${formatCount(page.total)} 条",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                TextButton(
                                    onClick = { vm.loadMore() },
                                    enabled = !vm.dimLoadingMore,
                                ) {
                                    if (vm.dimLoadingMore) {
                                        CircularProgressIndicator(
                                            Modifier.size(16.dp), color = Amber, strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.size(8.dp))
                                    }
                                    Text(
                                        if (vm.dimLoadingMore) "正在向上游取数…" else "加载更多",
                                        color = Amber,
                                    )
                                }
                                Text(
                                    "上游有频率限制，一次最多再取 500 条",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RowCard(index: Int, row: DimRow) {
    var expanded by remember { mutableStateOf(false) }
    val cells = row.cells.filter { it.second.isNotBlank() }
    if (cells.isEmpty()) return

    val titleKey = pickTitleKey(cells)
    val titleIdx = cells.indexOfFirst { it.first == titleKey }.coerceAtLeast(0)
    val titleValue = cells[titleIdx].second
    val others = cells.filterIndexed { i, _ -> i != titleIdx }
    val shown = if (expanded) others else others.take(3)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text("$index", fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                titleValue,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = if (expanded) 6 else 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        shown.forEach { (k, v) ->
            Row(Modifier.padding(top = 3.dp)) {
                Text(
                    Fields.label(k),
                    modifier = Modifier.size(width = 80.dp, height = 18.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    v,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) 20 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (others.size > 3) {
            Text(
                if (expanded) "收起" else "展开全部 ${cells.size} 项",
                style = MaterialTheme.typography.labelSmall,
                color = Amber,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** 选一个最能代表这行的字段作为标题 */
private fun pickTitleKey(cells: List<Pair<String, String>>): String {
    val preferred = listOf(
        "position", "title", "caseNo", "caseReason", "patentName", "tmName",
        "softwareName", "workerName", "shareholderName", "entName", "name",
        "changeItem", "punishReason", "domain", "websiteName", "bookName",
    )
    preferred.forEach { p -> if (cells.any { it.first == p && it.second.isNotBlank() }) return p }
    return cells.first().first
}
