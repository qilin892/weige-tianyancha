package com.weige.tianyancha.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weige.tianyancha.net.HotCompany
import com.weige.tianyancha.net.Suggestion
import com.weige.tianyancha.vm.MainViewModel

@Composable
fun SearchScreen(vm: MainViewModel, dark: Boolean, onToggleTheme: () -> Unit) {
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
        Spacer(Modifier.height(14.dp))
        BrandHeader(dark, onToggleTheme)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = vm.query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("输入企业名、法人、品牌…", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = accent()) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = vm::clearQuery) {
                        Icon(
                            Icons.Filled.Clear, "清空",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                focusedBorderColor = accent(),
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
        }

        Spacer(Modifier.height(28.dp))
    }
}

/** 品牌头：天眼标记 + 名称 + 深浅色切换 */
@Composable
private fun BrandHeader(dark: Boolean, onToggleTheme: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        EyeMark(Modifier.size(46.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                "伟哥天眼查",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                "企业信息 · 一查到底",
                style = MaterialTheme.typography.bodySmall,
                color = accent(),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        IconButton(onClick = onToggleTheme) {
            Icon(
                if (dark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                if (dark) "切换为白天模式" else "切换为夜间模式",
                tint = accent(),
            )
        }
    }
}

/**
 * 天眼标记：杏仁眼眶 + 琥珀虹膜 + 虹膜里的深色 W
 *
 * 与桌面图标同一套设计：W 既是 WeiGe 的 W，也像一条数据趋势线。
 * 用 Canvas 画，任意尺寸都清晰，不依赖图片资源。
 */
@Composable
fun EyeMark(modifier: Modifier = Modifier, ringColor: Color = Amber) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val cy = h / 2
        val strokeW = w * 0.055f
        val ink = Color(0xFF0B0D12)

        // 眼眶：控制点拉到画布上下边缘，二次曲线的顶点正好落在 0.25h / 0.75h
        val eye = Path().apply {
            moveTo(w * 0.03f, cy)
            quadraticTo(w * 0.5f, cy - 0.5f * h, w * 0.97f, cy)
            quadraticTo(w * 0.5f, cy + 0.5f * h, w * 0.03f, cy)
            close()
        }
        drawPath(eye, color = ringColor, style = Stroke(width = strokeW))

        // 虹膜：必须明显小于眼眶半高，否则小尺寸下会糊成一团橘色
        val r = w * 0.150f
        drawCircle(color = ringColor, radius = r, center = Offset(w / 2, cy))

        // 虹膜里的 W（品牌 + 数据趋势线）
        val wPath = Path().apply {
            moveTo(w / 2 - r * 0.60f, cy - r * 0.30f)
            lineTo(w / 2 - r * 0.22f, cy + r * 0.44f)
            lineTo(w / 2, cy - r * 0.10f)
            lineTo(w / 2 + r * 0.22f, cy + r * 0.44f)
            lineTo(w / 2 + r * 0.60f, cy - r * 0.30f)
        }
        drawPath(
            wPath,
            color = ink,
            style = Stroke(
                width = r * 0.30f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // 眼角两枚数据节点
        val nodeR = w * 0.022f
        drawCircle(ringColor, nodeR, Offset(w * 0.03f, cy))
        drawCircle(ringColor, nodeR, Offset(w * 0.97f, cy))
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
                    Modifier.size(16.dp), color = accent(), strokeWidth = 2.dp
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
        if (vm.suggestError != null) {
            EmptyBlock("搜索出错了", vm.suggestError)
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
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
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
        CompanyAvatar(s.logo, s.name, 34.dp, corner = 9.dp)
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
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

/** 热门查询：只是「点一下就能搜」的快捷入口，不展示任何统计数字 */
@Composable
private fun HotSection(hot: List<HotCompany>, onClick: (HotCompany) -> Unit) {
    if (hot.isEmpty()) return
    SectionCard(
        title = "热门查询",
        trailing = {
            Icon(
                Icons.Filled.TrendingUp, null,
                tint = accent().copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp),
            )
        },
    ) {
        hot.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                row.forEachIndexed { i, c ->
                    if (i > 0) Spacer(Modifier.width(8.dp))
                    HotChip(c, Modifier.weight(1f)) { onClick(c) }
                }
                if (row.size == 1) {
                    Spacer(Modifier.width(8.dp))
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CompanyAvatar(c.logo, c.name, 18.dp, corner = 5.dp)
            Text(
                c.name,
                modifier = Modifier.padding(start = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** 大数字缩写：1620 → 1620，16200 → 1.6万 */
fun formatCount(n: Int): String = when {
    n >= 10000 -> String.format("%.1f万", n / 10000.0)
    else -> n.toString()
}
