package com.vizoedit.studio.render

import android.content.Context
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.AudioEncoderSettings
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.vizoedit.studio.settings.EditorSettings
import com.vizoedit.studio.settings.OutputCodec

/** Builds a real Media3 Transformer with requested codec and nominal bitrates.
 * Android may select a compatible fallback encoder when the requested one is unavailable.
 */
@OptIn(UnstableApi::class)
object Media3ExportFactory {
    fun createBuilder(context: Context, settings: EditorSettings): Transformer.Builder {
        val safe = settings.validate()
        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder()
                    .setBitrate(safe.outputVideoBitrateBitsPerSecond)
                    .setiFrameIntervalSeconds(1f)
                    .build()
            )
            .setRequestedAudioEncoderSettings(
                AudioEncoderSettings.Builder()
                    .setBitrate(safe.outputAudioBitrateBitsPerSecond)
                    .build()
            )
            .build()

        val mimeType = when (OutputCodec.fromId(safe.outputCodecId)) {
            OutputCodec.H264 -> MimeTypes.VIDEO_H264
            OutputCodec.H265 -> MimeTypes.VIDEO_H265
        }
        return Transformer.Builder(context)
            .setEncoderFactory(encoderFactory)
            .setVideoMimeType(mimeType)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
    }
}
