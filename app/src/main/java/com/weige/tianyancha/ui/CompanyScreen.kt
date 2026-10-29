package com.weige.tianyancha.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weige.tianyancha.export.ExcelExporter
import com.weige.tianyancha.net.Company
import com.weige.tianyancha.net.DimItem
import com.weige.tianyancha.vm.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(vm: MainViewModel, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    var expandedInfo by remember { mutableStateOf(false) }
    val c = vm.company

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        c?.name ?: "企业详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, "返回",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refreshCompany() }) {
                        Icon(Icons.Filled.Refresh, "刷新", tint = accent())
                    }
                    IconButton(
                        enabled = c != null && !exporting,
                        onClick = {
                            val co = c ?: return@IconButton
                            scope.launch {
                                exporting = true
                                try {
                                    val r = ExcelExporter.export(ctx, co.entid, co.name)
                                    Toast.makeText(
                                        ctx, "已导出：${r.savedPath ?: r.displayName}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        ctx, "导出失败：${e.message}", Toast.LENGTH_LONG
                                    ).show()
                                } finally {
                                    exporting = false
                                }
                            }
                        }
                    ) {
                        if (exporting) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp), color = accent(), strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.FileDownload, "导出 Excel", tint = accent())
                        }
                    }
                    IconButton(
                        enabled = c != null && !exporting,
                        onClick = {
                            val co = c ?: return@IconButton
                            scope.launch {
                                try {
                                    ExcelExporter.share(ctx, co.entid, co.name)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        ctx, "分享失败：${e.message}", Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, "分享 Excel", tint = accent())
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
                vm.companyError != null -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    EmptyBlock("加载失败", vm.companyError)
                    Text(
                        "重试",
                        color = accent(),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { vm.refreshCompany() }
                            .padding(12.dp),
                    )
                }

                c == null -> LoadingBlock("正在获取企业信息…")
                else -> CompanyContent(vm, c, expandedInfo) { expandedInfo = !expandedInfo }
            }
        }
    }
}

@Composable
private fun CompanyContent(vm: MainViewModel, c: Company, expanded: Boolean, onToggle: () -> Unit) {
    val info = Fields.ordered(c.jbxx)
    val previewCount = 10
    val shown = if (expanded) info else info.take(previewCount)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        NameCard(c)
        Spacer(Modifier.height(12.dp))

        SectionCard(title = "工商信息") {
            shown.forEach { (k, v) -> FieldRow(k, v) }
            if (info.size > previewCount) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggle)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (expanded) "收起" else "展开全部",
                        style = MaterialTheme.typography.bodySmall,
                        color = accent(),
                    )
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        null, tint = accent(), modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        DimensionSection(c) { vm.openDimension(it) }
        Spacer(Modifier.height(20.dp))
    }
}

/** 企业名片：名称 + 信用代码 + 状态 + 四个关键要点（都配图标） */
@Composable
private fun NameCard(c: Company) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EyeMark(Modifier.size(34.dp))
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    c.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (c.uniscid.isNotBlank()) {
                    Text(
                        c.uniscid,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
            StatusChip(c.status)
        }

        // 关键要点：只挑用户最关心的四条，直接取自工商信息
        val highlights = listOf("personName", "regConcat", "esDate", "dom")
        val map = c.jbxx.toMap()
        val picked = highlights.mapNotNull { k -> map[k]?.let { k to it } }
            .filter { Fields.showable(it.first, it.second) }
        if (picked.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                picked.forEach { (k, v) -> FieldRow(k, v, labelWidth = 68, compact = true) }
            }
        }
    }
}

@Composable
private fun DimensionSection(c: Company, onClick: (DimItem) -> Unit) {
    SectionCard(title = "企业维度") {
        c.groups.forEach { (group, items) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    group,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent(),
                    fontWeight = FontWeight.SemiBold,
                )
                Box(
                    Modifier
                        .padding(start = 8.dp)
                        .height(1.dp)
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                )
            }
            items.forEach { d -> DimensionRow(d) { onClick(d) } }
        }
    }
}

@Composable
private fun DimensionRow(d: DimItem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Fields.dimensionIcon(d.name), null,
            tint = iconTint(),
            modifier = Modifier.size(15.dp),
        )
        Text(
            d.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            formatCount(d.count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            " 条",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp),
        )
    }
}
