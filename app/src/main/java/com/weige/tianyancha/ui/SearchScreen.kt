package com.weige.tianyancha.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weige.tianyancha.net.HotCompany
import com.weige.tianyancha.net.Stats
import com.weige.tianyancha.net.Suggestion
import com.weige.tianyancha.vm.MainViewModel

@Composable
fun SearchScreen(vm: MainViewModel) {
    val kb = LocalSoftwareKeyboardController.current

    Column(
        Modifier
            .fillMaxSize()
            // 边到边显示下自己补状态栏与输入法内边距，否则品牌头会被状态栏压住
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        BrandHeader()
        Spacer(Modifier.height(22.dp))

        OutlinedTextField(
            value = vm.query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入企业名、法人、品牌…", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = Amber) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = vm::clearQuery) {
                        Icon(Icons.Filled.Clear, "清空",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                vm.submitSearch()
                kb?.hide()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        if (vm.showSuggestions) {
            Spacer(Modifier.height(10.dp))
            SuggestionPanel(vm)
        } else {
            Spacer(Modifier.height(22.dp))
            HotSection(vm.hot) { c -> vm.openCompany(c.entid, c.name) }
            Spacer(Modifier.height(16.dp))
            StatsStrip(vm.stats)
        }

        Spacer(Modifier.height(28.dp))
        Text(
            "数据来自风鸟企业查询平台，本应用仅做聚合与缓存，结果以工商公示为准。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** 品牌头：天眼标记 + 名称 */
@Composable
private fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EyeMark(Modifier.size(52.dp))
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                "伟哥天眼查",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "企业信息 · 一查到底",
                style = MaterialTheme.typography.bodySmall,
                color = Amber,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/**
 * 天眼标记：杏仁眼眶 + 琥珀虹膜 + 内部三根柱状（企业数据）
 * 用 Canvas 画，任意尺寸都清晰，不依赖图片资源。
 */
@Composable
fun EyeMark(modifier: Modifier = Modifier, ringColor: Color = Amber, barColor: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2
        val stroke = w * 0.075f

        // 眼眶（上下两条二次曲线拼成杏仁形）
        val eye = Path().apply {
            moveTo(w * 0.04f, cy)
            quadraticTo(w * 0.5f, h * 0.06f, w * 0.96f, cy)
            quadraticTo(w * 0.5f, h * 0.94f, w * 0.04f, cy)
            close()
        }
        drawPath(eye, color = ringColor, style = Stroke(width = stroke))

        // 虹膜
        val r = w * 0.235f
        drawCircle(color = ringColor, radius = r, center = Offset(w / 2, cy))

        // 瞳孔
        drawCircle(color = Color(0xFF0B0D12), radius = r * 0.72f, center = Offset(w / 2, cy))

        // 内部三根递增柱（企业数据）
        val barW = (r * 2 * 0.62f) / 5f
        val bottom = cy + r * 0.42f
        listOf(0.34f, 0.58f, 0.86f).forEachIndexed { i, factor ->
            val bh = r * 1.05f * factor
            drawRect(
                color = barColor,
                topLeft = Offset(
                    w / 2 - barW * 2 + i * barW * 2,
                    bottom - bh
                ),
                size = Size(barW, bh),
            )
        }
    }
}

@Composable
private fun SuggestionPanel(vm: MainViewModel) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (vm.suggesting && vm.suggestions.isEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    Modifier.size(16.dp), color = Amber, strokeWidth = 2.dp
                )
                Text(
                    "正在检索…",
                    Modifier.padding(start = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }
        if (vm.suggestions.isEmpty()) {
            EmptyBlock("没有匹配的企业", "换个关键词试试，比如简称、法人名或品牌")
            return@Column
        }
        vm.suggestions.forEachIndexed { idx, s ->
            SuggestionRow(s) { vm.openCompany(s.entid, s.name) }
            if (idx != vm.suggestions.lastIndex) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(0.6.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(s: Suggestion, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                highlighted(s.segments, s.name),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (s.uniscid.isNotBlank()) {
                Text(
                    s.uniscid,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        StatusChip(s.status)
    }
}

@Composable
private fun HotSection(hot: List<HotCompany>, onClick: (HotCompany) -> Unit) {
    if (hot.isEmpty()) return
    SectionCard(title = "热门查询", subtitle = "${hot.size} 家") {
        hot.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                row.forEachIndexed { i, c ->
                    if (i > 0) Spacer(Modifier.size(8.dp))
                    HotChip(c, Modifier.weight(1f)) { onClick(c) }
                }
                if (row.size == 1) {
                    Spacer(Modifier.size(8.dp))
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HotChip(c: HotCompany, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column {
            Text(
                c.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (c.hitCount > 0) {
                Text(
                    "被查 ${c.hitCount} 次",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun StatsStrip(stats: Stats?) {
    if (stats == null) return
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatCell("已收录企业", stats.companies.toString())
        StatCell("维度数据", stats.dimensions.toString())
        StatCell("数据条目", formatCount(stats.dimensionRows))
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            color = Amber,
            fontWeight = FontWeight.Bold,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

fun formatCount(n: Int): String = when {
    n >= 10000 -> String.format("%.1f万", n / 10000.0)
    else -> n.toString()
}
