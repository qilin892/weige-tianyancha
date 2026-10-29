package com.weige.tianyancha.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weige.tianyancha.net.Seg

/** 经营状态小标签 */
@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    if (status.isBlank()) return
    val c = statusColor(status)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(c.copy(alpha = 0.14f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(status, color = c, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/** 带高亮的公司名（后端已把 HTML 拆成片段） */
fun highlighted(segments: List<Seg>, fallback: String): AnnotatedString =
    if (segments.isEmpty()) {
        AnnotatedString(fallback)
    } else buildAnnotatedString {
        segments.forEach { s ->
            if (s.hl) {
                withStyle(SpanStyle(color = Amber, fontWeight = FontWeight.Bold)) { append(s.t) }
            } else {
                append(s.t)
            }
        }
    }

/** 分区卡片标题 + 内容 */
@Composable
fun SectionCard(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 15.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent())
            )
            Text(
                title,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (trailing != null) {
                Box(Modifier.weight(1f))
                trailing()
            }
        }
        Column(Modifier.padding(top = 10.dp)) { content() }
    }
}

@Composable
fun LoadingBlock(text: String = "加载中…") {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = accent(), strokeWidth = 2.5.dp)
        Text(
            text,
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun EmptyBlock(title: String, hint: String? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            title, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hint != null) {
            Text(
                hint,
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * 字段行：左侧语义图标 + 中文标签，右侧按值类型渲染
 *
 * - 电话 → 点击直接拨号；邮箱 → 发信；网址 → 浏览器打开
 * - 状态 → 彩色标签；金额 → 加粗强调；日期 → 等宽显示
 * - 长文本 → 折叠，点击展开/收起
 */
@Composable
fun FieldRow(
    key: String,
    value: String,
    labelWidth: Int = 104,
    compact: Boolean = false,
    expandHint: Boolean = true,
    /** 由外层卡片统一控制长文本展开时传值，此时本行自己不再提供开关 */
    expandedOverride: Boolean? = null,
) {
    val kind = valueKind(key, value)
    var selfExpanded by remember(key, value) { mutableStateOf(false) }
    val expanded = expandedOverride ?: selfExpanded
    val iconSize = if (compact) 13.dp else 15.dp
    // 外层卡片接管展开时，本行不再自己响应点击（避免与卡片点击冲突）
    val selfToggle = if (expandedOverride == null) {
        Modifier.clickable { selfExpanded = !selfExpanded }
    } else Modifier

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 4.dp else 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val ic = Fields.iconOf(key)
        if (ic != null) {
            Icon(
                ic, null,
                tint = iconTint(),
                modifier = Modifier
                    .padding(top = if (compact) 1.dp else 2.dp)
                    .size(iconSize),
            )
        } else {
            // 没有图标的字段也留出同宽占位，保证所有标签左对齐
            Spacer(Modifier.size(iconSize))
        }
        Text(
            Fields.label(key),
            modifier = Modifier
                .padding(start = 6.dp)
                .width(labelWidth.dp),
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when (kind) {
            ValueKind.Status -> StatusChip(value)

            ValueKind.Bool -> {
                val yes = value == "是" || value.equals("true", true)
                Icon(
                    if (yes) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    null,
                    tint = if (yes) FallGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(iconSize + 2.dp),
                )
                Text(
                    value,
                    modifier = Modifier.padding(start = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            ValueKind.Phone -> LinkValue(
                value, "tel:${value.replace(Regex("[^\\d+]"), "")}",
                Icons.Filled.OpenInNew, Modifier.weight(1f),
            )
            ValueKind.Email -> LinkValue(value, "mailto:$value", Icons.Filled.OpenInNew, Modifier.weight(1f))
            ValueKind.Url -> LinkValue(
                value,
                if (value.startsWith("http")) value else "https://$value",
                Icons.Filled.OpenInNew,
                Modifier.weight(1f),
            )

            ValueKind.Money -> Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent(),
                modifier = Modifier.weight(1f),
            )

            ValueKind.Number, ValueKind.Date -> Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            ValueKind.Long -> Column(Modifier.weight(1f)) {
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = selfToggle,
                )
                if (expandHint) {
                    Text(
                        if (expanded) "收起" else "展开",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent(),
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .then(selfToggle),
                    )
                }
            }

            ValueKind.Text -> Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** 可点击的值（电话/邮箱/网址），末尾带一个"去处理"的小图标 */
@Composable
private fun LinkValue(
    text: String,
    uri: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    Row(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                runCatching {
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = accent(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Icon(
            icon, null,
            tint = accent().copy(alpha = 0.7f),
            modifier = Modifier
                .padding(start = 4.dp)
                .size(13.dp),
        )
    }
}

/** 兼容旧调用：只要一行键值（无图标）时用 */
@Composable
fun KeyValueRow(label: String, value: String, valueColor: Color? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(
            label,
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}
