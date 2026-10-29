package com.weige.tianyancha.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 企业 logo 加载
 *
 * 上游（风鸟）的 `entPic` 有两个形态：
 *  - `https://oss2.riskbird.com/bigdata/company-logo/...jpg!fengniaowater` —— 真 logo 图；
 *  - `裕普华泰` 这种纯文本 —— 该公司没有 logo，用名称前缀兜底。
 * 所以这里既要能取图，也要能退化成文字头像。
 */
object LogoCache {
    /** 按字节计上限，避免几十家企业就吃掉几十 MB */
    private val mem = object : LruCache<String, ImageBitmap>(8 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            value.width * value.height * 4
    }

    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    fun cached(url: String): ImageBitmap? = mem.get(url)

    suspend fun load(url: String): ImageBitmap? = withContext(Dispatchers.IO) {
        mem.get(url)?.let { return@withContext it }
        // CDN 会校验 UA，缺了容易拿到占位图
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/131.0.0.0")
            .build()
        runCatching {
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val bytes = resp.body?.bytes() ?: return@use null
                decodeSmall(bytes)?.asImageBitmap()
            }
        }.getOrNull()?.also { mem.put(url, it) }
    }

    /** 先读尺寸再按 2 的幂降采样，300×300 的图落到 150 左右就够显示了 */
    private fun decodeSmall(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= 160 && bounds.outHeight / (sample * 2) >= 160) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}

/** 拿到 logo 位图；非 http 开头（即上游给的是文本）直接返回 null */
@Composable
fun rememberLogo(logo: String?): ImageBitmap? {
    val url = logo?.trim()?.takeIf { it.startsWith("http") } ?: return null
    var img by remember(url) { mutableStateOf(LogoCache.cached(url)) }
    LaunchedEffect(url) {
        if (img == null) img = LogoCache.load(url)
    }
    return img
}

/**
 * 企业头像：有 logo 显示图片，没有就用名称前缀做文字头像
 *
 * 注意：**不要**用 App 品牌图标（天眼标）占位 —— 那是应用标识，不是企业标识。
 */
@Composable
fun CompanyAvatar(
    logo: String?,
    name: String,
    size: Dp,
    corner: Dp? = null,
) {
    val shape = RoundedCornerShape(corner ?: (size * 0.26f))
    val img = rememberLogo(logo)
    val text = avatarText(logo, name)

    Box(
        Modifier
            .size(size)
            .clip(shape)
            .background(
                if (img != null) MaterialTheme.colorScheme.surfaceVariant
                else accent().copy(alpha = 0.14f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (img != null) {
            Image(
                img,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape),
            )
        } else {
            Text(
                text,
                color = accent(),
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp,
                maxLines = 1,
            )
        }
    }
}

/** 文字头像取字：上游给的文本（如「裕普华泰」）优先，否则取企业名头两字 */
private fun avatarText(logo: String?, name: String): String {
    val raw = logo?.trim().orEmpty()
    val src = if (raw.isNotEmpty() && !raw.startsWith("http")) raw else name.trim()
    val clean = src.removeSuffix("有限公司").removeSuffix("有限责任公司").trim()
    val base = clean.ifEmpty { src }
    return base.take(2)
}
