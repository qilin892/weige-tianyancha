package com.weige.tianyancha.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 品牌色：与「伟哥」系列保持一致 —— 近黑底 + 琥珀强调 */
val Amber = Color(0xFFFF7A18)
val AmberSoft = Color(0xFFFFB067)

private val DarkBg = Color(0xFF0B0D12)
private val DarkPanel = Color(0xFF141821)
private val DarkPanelHigh = Color(0xFF1C2230)
private val TextMain = Color(0xFFF2F4F8)
private val TextSub = Color(0xFF9AA4B5)

val RiseRed = Color(0xFFF04C4C)     // 风险/异常提示用红（企业状态本身用绿，见 Fields.statusColor）
val FallGreen = Color(0xFF2BB673)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A0F00),
    primaryContainer = Color(0xFF3A2308),
    onPrimaryContainer = AmberSoft,
    secondary = AmberSoft,
    background = DarkBg,
    onBackground = TextMain,
    surface = DarkPanel,
    onSurface = TextMain,
    surfaceVariant = DarkPanelHigh,
    onSurfaceVariant = TextSub,
    outline = Color(0xFF2C3444),
    error = Color(0xFFFF5C5C),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFFD95F00),
    onPrimary = Color.White,
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF12151C),
    surface = Color.White,
    onSurface = Color(0xFF12151C),
    surfaceVariant = Color(0xFFEDEFF3),
    onSurfaceVariant = Color(0xFF5A6372),
    outline = Color(0xFFD6DAE2),
    error = Color(0xFFC62828),
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.5.sp),
    labelSmall = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun WeigeTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
