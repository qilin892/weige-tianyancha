package com.weige.tianyancha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
                    .background(Amber)
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
        CircularProgressIndicator(color = Amber, strokeWidth = 2.5.dp)
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
        Text(title, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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

/** 键值一行（左标签右值，值可换行） */
@Composable
fun KeyValueRow(label: String, value: String, valueColor: Color? = null) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Text(
            label,
            // 只定宽不定高：长标签（如「统一社会信用代码」）需要换行，定高会被裁掉
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
