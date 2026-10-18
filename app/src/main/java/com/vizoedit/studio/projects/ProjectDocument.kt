package com.vizoedit.studio.projects

import org.json.JSONObject
import com.vizoedit.studio.settings.EditorSettings
import com.vizoedit.studio.settings.OutputCanvas
import com.vizoedit.studio.settings.OverlayAnchor
import com.vizoedit.studio.settings.OutputCodec
import com.vizoedit.studio.settings.ColorLookPreset
import com.vizoedit.studio.settings.OverlayColor

/** Versioned, real project metadata document; source URIs are persisted as strings. */
data class ProjectDocument(
    val name: String,
    val sourceUri: String,
    val durationMs: Long,
    val trimStartMs: Long,
    val trimEndMs: Long,
    val previewSpeed: Float = 1f,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val editorSettings: EditorSettings = EditorSettings()
) {
    fun validate(): ProjectDocument {
        require(name.isNotBlank()) { "Project name cannot be blank" }
        require(sourceUri.isNotBlank()) { "Source URI is required" }
        require(durationMs > 0L) { "Duration must be positive" }
        require(trimStartMs >= 0L && trimEndMs <= durationMs && trimEndMs > trimStartMs) {
            "Trim range is invalid"
        }
        require(previewSpeed in 0.25f..4f) { "Preview speed must be between 0.25x and 4x" }
        require(schemaVersion == CURRENT_SCHEMA_VERSION) { "Unsupported project schema version" }
        editorSettings.validate()
        return this
    }

    fun toJson(): String = JSONObject()
        .put("schemaVersion", schemaVersion)
        .put("name", name)
        .put("sourceUri", sourceUri)
        .put("durationMs", durationMs)
        .put("trimStartMs", trimStartMs)
        .put("trimEndMs", trimEndMs)
        .put("previewSpeed", previewSpeed.toDouble())
        .put("editorSettings", JSONObject()
            .put("rotationDegrees", editorSettings.rotationDegrees.toDouble())
            .put("zoom", editorSettings.zoom.toDouble())
            .put("redScale", editorSettings.redScale.toDouble())
            .put("greenScale", editorSettings.greenScale.toDouble())
            .put("blueScale", editorSettings.blueScale.toDouble())
            .put("brightness", editorSettings.brightness.toDouble())
            .put("contrast", editorSettings.contrast.toDouble())
            .put("hueDegrees", editorSettings.hueDegrees.toDouble())
            .put("saturation", editorSettings.saturation.toDouble())
            .put("lightness", editorSettings.lightness.toDouble())
            .put("cropLeft", editorSettings.cropLeft.toDouble())
            .put("cropTop", editorSettings.cropTop.toDouble())
            .put("cropRight", editorSettings.cropRight.toDouble())
            .put("cropBottom", editorSettings.cropBottom.toDouble())
            .put("outputCanvasId", editorSettings.outputCanvasId)
            .put("overlayText", editorSettings.overlayText)
            .put("overlayAnchor", editorSettings.overlayAnchor.name)
            .put("overlayColorId", editorSettings.overlayColorId)
            .put("overlayOpacity", editorSettings.overlayOpacity.toDouble())
            .put("overlayRotationDegrees", editorSettings.overlayRotationDegrees.toDouble())
            .put("overlayScale", editorSettings.overlayScale.toDouble())
            .put("outputFrameRate", editorSettings.outputFrameRate)
            .put("outputCodecId", editorSettings.outputCodecId)
            .put("outputVideoBitrateBitsPerSecond", editorSettings.outputVideoBitrateBitsPerSecond)
            .put("outputAudioBitrateBitsPerSecond", editorSettings.outputAudioBitrateBitsPerSecond)
            .put("lookPresetId", editorSettings.lookPresetId)
            .put("sourceVolume", editorSettings.sourceVolume.toDouble())
            .put("audioFadeInMs", editorSettings.audioFadeInMs)
            .put("audioFadeOutMs", editorSettings.audioFadeOutMs)
            .put("muteOriginalAudio", editorSettings.muteOriginalAudio))
        .toString(2)

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        fun fromJson(raw: String): ProjectDocument {
            val json = JSONObject(raw)
            return ProjectDocument(
                name = json.getString("name"),
                sourceUri = json.getString("sourceUri"),
                durationMs = json.getLong("durationMs"),
                trimStartMs = json.getLong("trimStartMs"),
                trimEndMs = json.getLong("trimEndMs"),
                previewSpeed = json.optDouble("previewSpeed", 1.0).toFloat(),
                schemaVersion = json.getInt("schemaVersion"),
                editorSettings = json.optJSONObject("editorSettings")?.let { settings ->
                    EditorSettings(
                        rotationDegrees = settings.optDouble("rotationDegrees", 0.0).toFloat(),
                        zoom = settings.optDouble("zoom", 1.0).toFloat(),
                        redScale = settings.optDouble("redScale", 1.0).toFloat(),
                        greenScale = settings.optDouble("greenScale", 1.0).toFloat(),
                        blueScale = settings.optDouble("blueScale", 1.0).toFloat(),
                        brightness = settings.optDouble("brightness", 0.0).toFloat(),
                        contrast = settings.optDouble("contrast", 0.0).toFloat(),
                        hueDegrees = settings.optDouble("hueDegrees", 0.0).toFloat(),
                        saturation = settings.optDouble("saturation", 0.0).toFloat(),
                        lightness = settings.optDouble("lightness", 0.0).toFloat(),
                        cropLeft = settings.optDouble("cropLeft", 0.0).toFloat(),
                        cropTop = settings.optDouble("cropTop", 0.0).toFloat(),
                        cropRight = settings.optDouble("cropRight", 0.0).toFloat(),
                        cropBottom = settings.optDouble("cropBottom", 0.0).toFloat(),
                        outputCanvasId = settings.optString("outputCanvasId", OutputCanvas.SOURCE.id),
                        overlayText = settings.optString("overlayText", ""),
                        overlayAnchor = runCatching { OverlayAnchor.valueOf(settings.optString("overlayAnchor", OverlayAnchor.BOTTOM.name)) }.getOrDefault(OverlayAnchor.BOTTOM),
                        overlayColorId = settings.optString("overlayColorId", OverlayColor.WHITE.id),
                        overlayOpacity = settings.optDouble("overlayOpacity", 1.0).toFloat(),
                        overlayRotationDegrees = settings.optDouble("overlayRotationDegrees", 0.0).toFloat(),
                        overlayScale = settings.optDouble("overlayScale", 1.0).toFloat(),
                        outputFrameRate = settings.optInt("outputFrameRate", 30),
                        outputCodecId = settings.optString("outputCodecId", OutputCodec.H264.id),
                        outputVideoBitrateBitsPerSecond = settings.optInt("outputVideoBitrateBitsPerSecond", 8_000_000),
                        outputAudioBitrateBitsPerSecond = settings.optInt("outputAudioBitrateBitsPerSecond", 192_000),
                        lookPresetId = settings.optString("lookPresetId", ColorLookPreset.NORMAL.id),
                        sourceVolume = settings.optDouble("sourceVolume", 1.0).toFloat(),
                        audioFadeInMs = settings.optLong("audioFadeInMs", 0L),
                        audioFadeOutMs = settings.optLong("audioFadeOutMs", 0L),
                        muteOriginalAudio = settings.optBoolean("muteOriginalAudio", false)
                    )
                } ?: EditorSettings()
            ).validate()
        }
    }
}
