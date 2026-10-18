package com.vizoedit.studio.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.IOException
import java.io.OutputStream

/** Extracts a real frame from a selected video URI. Caller owns the output stream. */
object FrameExtractor {
    @Throws(IOException::class)
    fun writePng(context: Context, source: Uri, timeMs: Long, output: OutputStream) {
        val retriever = MediaMetadataRetriever()
        var bitmap: Bitmap? = null
        try {
            retriever.setDataSource(context, source)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: throw IOException("Unable to read video duration")
            require(timeMs in 0..duration) { "Frame time is outside the video duration" }
            bitmap = retriever.getFrameAtTime(timeMs * 1_000L, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: throw IOException("Unable to decode a frame at the selected time")
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                throw IOException("PNG encoding failed")
            }
        } catch (e: IllegalArgumentException) {
            throw IOException("Unsupported or unreadable video", e)
        } finally {
            bitmap?.recycle()
            try { retriever.release() } catch (_: RuntimeException) { }
        }
    }
}
