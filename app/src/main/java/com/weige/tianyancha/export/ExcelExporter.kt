package com.weige.tianyancha.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.weige.tianyancha.net.Api
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Excel 导出与分享
 *
 * 两条路径刻意分开：
 *  - **导出**：Android 10+ 直接写系统「下载/伟哥天眼查/」，用 MediaStore，全程零权限；
 *            Android 8/9 写 app 私有外部目录（写公共目录要 WRITE_EXTERNAL_STORAGE，
 *            为一个导出功能弹权限框不划算）。
 *  - **分享**：一律先落 app 私有目录，再经 FileProvider 授权给微信/QQ/邮件，
 *            避免把 MediaStore 的 uri 直接抛给第三方 App 出现权限问题。
 */
object ExcelExporter {

    private const val MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    private const val SUB_DIR = "伟哥天眼查"

    data class Result(val displayName: String, val uri: Uri?, val savedPath: String?)

    private fun fileName(companyName: String): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(Date())
        val safe = companyName.replace(Regex("""[\\/:*?"<>|\s]+"""), "_").take(40)
            .ifBlank { "企业" }
        return "${safe}_企业信息_$stamp.xlsx"
    }

    private fun cleanName(name: String): String =
        name.replace(Regex("""[\\/:*?"<>|\s]+"""), "_").take(40).ifBlank { "企业" }

    /**
     * 导出到手机「下载」目录（Android 10+ 走 MediaStore，可在文件管理里直接看到）
     */
    suspend fun export(context: Context, entid: String, companyName: String): Result {
        val bytes = Api.exportExcel(entid)
        val name = fileName(companyName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, name)
                put(MediaStore.Downloads.MIME_TYPE, MIME)
                put(MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$SUB_DIR")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("无法在下载目录创建文件")
            try {
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("写入失败")
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } catch (e: Exception) {
                runCatching { resolver.delete(uri, null, null) }
                throw e
            }
            val shown = "下载/$SUB_DIR/$name"
            return Result(name, uri, shown)
        }

        // Android 8/9：写 app 私有外部目录
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val file = File(dir, name)
        file.writeBytes(bytes)
        return Result(name, null, file.absolutePath)
    }

    /**
     * 分享：先落私有目录，再交给系统分享面板
     */
    suspend fun share(context: Context, entid: String, companyName: String) {
        val bytes = Api.exportExcel(entid)
        val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        // 分享用的文件名去掉时间戳更干净，但同企业多次分享要覆盖 → 保留时间戳避免歧义
        val file = File(dir, fileName(companyName))
        file.writeBytes(bytes)

        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${cleanName(companyName)} 企业信息")
            putExtra(Intent.EXTRA_TEXT, "由「伟哥天眼查」导出：${cleanName(companyName)}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "分享企业信息表格").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }
}
