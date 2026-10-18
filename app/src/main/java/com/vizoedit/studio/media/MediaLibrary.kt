package com.vizoedit.studio.media

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException

/** Copies a completed video into the user's Movies/VizoEdit collection on Android 10+. */
object MediaLibrary {
    @Throws(IOException::class)
    fun saveVideoToGallery(context: Context, source: File): Uri {
        require(source.isFile && source.length() > 0L) { "Exported video is missing or empty" }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            throw IOException("حفظ المعرض المباشر يتطلب Android 10 أو أحدث. ملف التصدير الأصلي ما زال محفوظًا داخل مجلد التطبيق.")
        }
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, source.nameWithoutExtension + "_gallery.mp4")
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VizoEdit")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val destination = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("تعذر إنشاء ملف في مكتبة الفيديو")
        try {
            val output = resolver.openOutputStream(destination, "w")
                ?: throw IOException("تعذر فتح ملف المعرض للكتابة")
            source.inputStream().use { input -> output.use { input.copyTo(it) } }
            val publish = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
            if (resolver.update(destination, publish, null, null) <= 0) {
                throw IOException("تعذر إتاحة الفيديو في المعرض")
            }
            return destination
        } catch (e: Exception) {
            try { resolver.delete(destination, null, null) } catch (_: Exception) { }
            if (e is IOException) throw e
            throw IOException("فشل نسخ الفيديو إلى المعرض", e)
        }
    }
}
