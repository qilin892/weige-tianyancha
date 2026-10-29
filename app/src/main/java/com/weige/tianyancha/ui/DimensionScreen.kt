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
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (page != null) {
                            Text(
                                "共 ${formatCount(page.total)} 条",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
                    "该维度暂时没有记录，可返回上一页刷新后重试",
                )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    itemsIndexed(page.rows) { idx, row ->
                        RowCard(idx + 1, title, row)
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
                                            Modifier.size(16.dp), color = accent(), strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.size(8.dp))
                                    }
                                    Text(
                                        if (vm.dimLoadingMore) "正在获取…" else "加载更多",
                                        color = accent(),
                                    )
                                }
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
private fun RowCard(index: Int, dimensionName: String, row: DimRow) {
    var expanded by remember { mutableStateOf(false) }
    val cells = Fields.visibleCells(row.cells)
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
            Icon(
                Fields.dimensionIcon(dimensionName), null,
                tint = iconTint(),
                modifier = Modifier.padding(top = 1.dp).size(15.dp),
            )
            Text(
                titleValue,
                modifier = Modifier.padding(start = 6.dp).weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) 6 else 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "#$index",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        }
        Spacer(Modifier.height(4.dp))
        shown.forEach { (k, v) ->
            FieldRow(
                key = k, value = v, labelWidth = 72, compact = true,
                expandHint = false, expandedOverride = expanded,
            )
        }
        if (others.size > 3) {
            Text(
                if (expanded) "收起" else "展开全部",
                style = MaterialTheme.typography.labelSmall,
                color = accent(),
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

/** 选一个最能代表这行的字段作为标题 */
private fun pickTitleKey(cells: List<Pair<String, String>>): String {
    val preferred = listOf(
        // 名称型字段优先，直接决定这张卡「叫什么」
        "standardName", "patentName", "tmName", "softwareName", "workName",
        "shareHolderName", "shareholderName", "shaName", "controllerName",
        "beneficiaryName", "benefitName", "personName", "entName", "name",
        "wname", "websiteName", "domain", "title", "tenderTitle", "jobRequirement",
        // 再说「事件型」维度，案号/事由/处罚内容才是主信息
        "caseNo", "caseReason", "cause", "punishContent", "punishNo",
        "altItemCodeCn", "altItem", "addReason", "checkResult", "mortgagee", "pledgee",
        "position", "type", "status",
    )
    preferred.forEach { p -> if (cells.any { it.first == p && it.second.isNotBlank() }) return p }
    // 兜底：挑第一个「像名字」的文本（有长度、含中文），避免拿 id、金额当标题
    cells.firstOrNull { (_, v) ->
        v.length >= 4 && v.any { c -> c.isLetter() && c.code > 0x2E80 }
    }?.let { return it.first }
    return cells.first().first
}
