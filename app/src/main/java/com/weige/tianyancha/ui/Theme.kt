package com.weige.tianyancha.ui

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 品牌色：琥珀（图标与强调色，深浅色模式通用） */
val Amber = Color(0xFFFF7A18)
val AmberSoft = Color(0xFFFFB067)
/** 浅色模式下的"文字用琥珀"：纯 Amber 在白底上对比度不够 */
val AmberDeep = Color(0xFFC4530A)

val RiseRed = Color(0xFFF04C4C)     // 风险/异常提示用红
val FallGreen = Color(0xFF2E9E62)   // 正常经营

// ---------------------------------------------------------------- 深色
private val DarkBg = Color(0xFF0B0D12)
private val DarkPanel = Color(0xFF141821)
private val DarkPanelHigh = Color(0xFF1C2230)
private val TextMainDark = Color(0xFFF2F4F8)
private val TextSubDark = Color(0xFF9AA4B5)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF1A0F00),
    primaryContainer = Color(0xFF3A2308),
    onPrimaryContainer = AmberSoft,
    secondary = AmberSoft,
    background = DarkBg,
    onBackground = TextMainDark,
    surface = DarkPanel,
    onSurface = TextMainDark,
    surfaceVariant = DarkPanelHigh,
    onSurfaceVariant = TextSubDark,
    outline = Color(0xFF2C3444),
    error = Color(0xFFFF5C5C),
)

// ---------------------------------------------------------------- 浅色（默认）
private val LightBg = Color(0xFFF4F6F9)
private val LightPanel = Color(0xFFFFFFFF)
private val LightPanelHigh = Color(0xFFEDF1F6)
private val TextMainLight = Color(0xFF15181F)
private val TextSubLight = Color(0xFF5C6675)

private val LightColors = lightColorScheme(
    primary = AmberDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE8D6),
    onPrimaryContainer = Color(0xFF7A3200),
    secondary = AmberDeep,
    background = LightBg,
    onBackground = TextMainLight,
    surface = LightPanel,
    onSurface = TextMainLight,
    surfaceVariant = LightPanelHigh,
    onSurfaceVariant = TextSubLight,
    outline = Color(0xFFDCE2EB),
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
fun WeigeTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}

/** 强调色：跟随深浅色模式取对比度合适的那一档 */
@Composable
fun accent(): Color = MaterialTheme.colorScheme.primary

/** 图标配色（比强调色淡一档，避免抢正文） */
@Composable
fun iconTint(): Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)

// ---------------------------------------------------------------- 深浅色偏好

/** 主题偏好落 SharedPreferences：默认白天模式，可切夜间模式 */
object ThemePref {
    private const val FILE = "weige_tianyancha"
    private const val KEY_DARK = "dark_mode"

    fun isDark(ctx: Context): Boolean =
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)

    fun setDark(ctx: Context, dark: Boolean) {
        ctx.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_DARK, dark).apply()
    }
}
