package com.vizoedit.studio.render

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import androidx.media3.common.Effect
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.DefaultGainProvider
import androidx.media3.common.audio.GainProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Crop
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.Presentation
import androidx.media3.effect.RgbAdjustment
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextOverlay
import androidx.media3.effect.TextureOverlay
import com.vizoedit.studio.settings.EditorSettings
import com.vizoedit.studio.settings.OutputCanvas
import com.vizoedit.studio.settings.ColorLookPreset
import com.vizoedit.studio.settings.OverlayColor

/** Creates real Media3 frame effects for export. It intentionally adds no effect when a control is at its default. */
@OptIn(UnstableApi::class)
object Media3EffectFactory {
    fun createAudioProcessors(settings: EditorSettings, clipDurationMs: Long): List<AudioProcessor> {
        settings.validate()
        require(clipDurationMs > 0L) { "Clip duration must be positive" }
        if (settings.muteOriginalAudio) return emptyList()
        val fadeInMs = settings.audioFadeInMs.coerceAtMost(clipDurationMs)
        val fadeOutMs = settings.audioFadeOutMs.coerceAtMost((clipDurationMs - fadeInMs).coerceAtLeast(0L))
        if (settings.sourceVolume == 1f && fadeInMs == 0L && fadeOutMs == 0L) return emptyList()
        val gain = DefaultGainProvider.Builder(settings.sourceVolume).apply {
            if (fadeInMs > 0L) addFadeAt(0L, fadeInMs * 1_000L, DefaultGainProvider.FADE_IN_LINEAR)
            if (fadeOutMs > 0L) addFadeAt((clipDurationMs - fadeOutMs).coerceAtLeast(0L) * 1_000L, fadeOutMs * 1_000L, DefaultGainProvider.FADE_OUT_LINEAR)
        }.build()
        return listOf(GainProcessor(gain))
    }

    fun createVideoEffects(settings: EditorSettings): List<Effect> {
        settings.validate()
        val effective = ColorLookPreset.fromId(settings.lookPresetId).applyTo(settings).validate()
        val effects = mutableListOf<Effect>()

        if (effective.rotationDegrees != 0f || effective.zoom != 1f) {
            effects += ScaleAndRotateTransformation.Builder()
                .setRotationDegrees(effective.rotationDegrees)
                .setScale(effective.zoom, effective.zoom)
                .build()
        }

        if (effective.redScale != 1f || effective.greenScale != 1f || effective.blueScale != 1f) {
            effects += RgbAdjustment.Builder()
                .setRedScale(effective.redScale)
                .setGreenScale(effective.greenScale)
                .setBlueScale(effective.blueScale)
                .build()
        }
        if (effective.brightness != 0f) effects += Brightness(effective.brightness)
        if (effective.contrast != 0f) effects += Contrast(effective.contrast)
        if (effective.hueDegrees != 0f || effective.saturation != 0f || effective.lightness != 0f) {
            effects += HslAdjustment.Builder()
                .adjustHue(effective.hueDegrees)
                .adjustSaturation(effective.saturation)
                .adjustLightness(effective.lightness)
                .build()
        }

        if (effective.cropLeft > 0f || effective.cropTop > 0f || effective.cropRight > 0f || effective.cropBottom > 0f) {
            effects += Crop(
                -1f + 2f * effective.cropLeft,
                1f - 2f * effective.cropRight,
                -1f + 2f * effective.cropBottom,
                1f - 2f * effective.cropTop
            )
        }

        val canvas = OutputCanvas.fromId(effective.outputCanvasId)
        if (canvas != OutputCanvas.SOURCE) {
            effects += Presentation.createForWidthAndHeight(
                canvas.width,
                canvas.height,
                Presentation.LAYOUT_SCALE_TO_FIT
            )
        }

        if (effective.overlayText.isNotBlank()) {
            val styledText = SpannableString(effective.overlayText.trim()).apply {
                setSpan(ForegroundColorSpan(OverlayColor.fromId(effective.overlayColorId).argb), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val overlaySettings = StaticOverlaySettings.Builder()
                .setBackgroundFrameAnchor(OverlayAnchorCoordinates.x(effective), OverlayAnchorCoordinates.y(effective))
                .setOverlayFrameAnchor(0f, 0f)
                .setScale(effective.overlayScale, effective.overlayScale)
                .setAlphaScale(effective.overlayOpacity)
                .setRotationDegrees(effective.overlayRotationDegrees)
                .build()
            val overlay: TextureOverlay = TextOverlay.createStaticTextOverlay(styledText, overlaySettings)
            effects += OverlayEffect(listOf(overlay))
        }
        return effects.toList()
    }
}

private object OverlayAnchorCoordinates {
    fun x(settings: EditorSettings): Float = settings.overlayAnchor.x
    fun y(settings: EditorSettings): Float = settings.overlayAnchor.y
}
