package com.vizoedit.studio.settings

/** Settings that are consumed by the preview UI and the Media3 render pipeline. */
data class EditorSettings(
    val rotationDegrees: Float = 0f,
    val zoom: Float = 1f,
    val redScale: Float = 1f,
    val greenScale: Float = 1f,
    val blueScale: Float = 1f,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val hueDegrees: Float = 0f,
    val saturation: Float = 0f,
    val lightness: Float = 0f,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 0f,
    val cropBottom: Float = 0f,
    val outputCanvasId: String = OutputCanvas.SOURCE.id,
    val overlayText: String = "",
    val overlayAnchor: OverlayAnchor = OverlayAnchor.BOTTOM,
    val overlayColorId: String = OverlayColor.WHITE.id,
    val overlayOpacity: Float = 1f,
    val overlayRotationDegrees: Float = 0f,
    val overlayScale: Float = 1f,
    val outputFrameRate: Int = 30,
    val outputCodecId: String = OutputCodec.H264.id,
    val outputVideoBitrateBitsPerSecond: Int = 8_000_000,
    val outputAudioBitrateBitsPerSecond: Int = 192_000,
    val lookPresetId: String = ColorLookPreset.NORMAL.id,
    val sourceVolume: Float = 1f,
    val audioFadeInMs: Long = 0L,
    val audioFadeOutMs: Long = 0L,
    val muteOriginalAudio: Boolean = false
) {
    fun validate(): EditorSettings {
        require(rotationDegrees.isFinite() && rotationDegrees in -180f..180f) {
            "Rotation must be between -180 and 180 degrees"
        }
        require(zoom.isFinite() && zoom in 1f..2f) { "Zoom must be between 1x and 2x" }
        require(redScale.isFinite() && redScale in 0.5f..1.5f) { "Red channel scale must be between 0.5 and 1.5" }
        require(greenScale.isFinite() && greenScale in 0.5f..1.5f) { "Green channel scale must be between 0.5 and 1.5" }
        require(blueScale.isFinite() && blueScale in 0.5f..1.5f) { "Blue channel scale must be between 0.5 and 1.5" }
        require(brightness.isFinite() && brightness in -1f..1f) { "Brightness must be between -1 and 1" }
        require(contrast.isFinite() && contrast in -1f..1f) { "Contrast must be between -1 and 1" }
        require(hueDegrees.isFinite() && hueDegrees in -180f..180f) { "Hue must be between -180 and 180 degrees" }
        require(saturation.isFinite() && saturation in -100f..100f) { "Saturation must be between -100 and 100" }
        require(lightness.isFinite() && lightness in -100f..100f) { "Lightness must be between -100 and 100" }
        require(listOf(cropLeft, cropTop, cropRight, cropBottom).all { it.isFinite() && it in 0f..0.45f }) {
            "Crop edges must be between 0 and 45 percent"
        }
        require(cropLeft + cropRight < 0.9f && cropTop + cropBottom < 0.9f) { "Crop removes too much of the frame" }
        require(OutputCanvas.entries.any { it.id == outputCanvasId }) { "Unknown output canvas preset" }
        require(overlayText.length <= MAX_OVERLAY_TEXT_LENGTH) { "Overlay text is too long" }
        require(OverlayColor.entries.any { it.id == overlayColorId }) { "Unknown overlay color" }
        require(overlayOpacity.isFinite() && overlayOpacity in 0f..1f) { "Overlay opacity must be between 0 and 1" }
        require(overlayRotationDegrees.isFinite() && overlayRotationDegrees in -180f..180f) { "Overlay rotation must be between -180 and 180 degrees" }
        require(overlayScale.isFinite() && overlayScale in 0.4f..2f) { "Overlay scale must be between 0.4 and 2" }
        require(outputFrameRate in ALLOWED_FRAME_RATES) { "Unsupported output frame rate" }
        require(OutputCodec.entries.any { it.id == outputCodecId }) { "Unknown output codec" }
        require(outputVideoBitrateBitsPerSecond in 250_000..100_000_000) { "Video bitrate must be between 250 kbps and 100 Mbps" }
        require(outputAudioBitrateBitsPerSecond in 32_000..512_000) { "Audio bitrate must be between 32 and 512 kbps" }
        require(ColorLookPreset.entries.any { it.id == lookPresetId }) { "Unknown color look preset" }
        require(sourceVolume.isFinite() && sourceVolume in 0f..1f) { "Source volume must be between 0 and 100 percent" }
        require(audioFadeInMs in 0L..10_000L && audioFadeOutMs in 0L..10_000L) { "Audio fades must be between 0 and 10000 ms" }
        return this
    }

    companion object {
        const val MAX_OVERLAY_TEXT_LENGTH = 240
        val ALLOWED_FRAME_RATES = setOf(24, 25, 30, 50, 60)
    }
}

enum class OutputCanvas(val id: String, val title: String, val width: Int, val height: Int) {
    SOURCE("SOURCE", "أصلية", 0, 0),
    LANDSCAPE_16_9("16_9", "16:9", 1920, 1080),
    PORTRAIT_9_16("9_16", "9:16", 1080, 1920),
    SQUARE_1_1("1_1", "1:1", 1080, 1080),
    PORTRAIT_4_5("4_5", "4:5", 1080, 1350),
    CLASSIC_4_3("4_3", "4:3", 1440, 1080),
    CINEMA_21_9("21_9", "21:9", 2520, 1080);

    companion object {
        fun fromId(id: String): OutputCanvas = entries.firstOrNull { it.id == id } ?: SOURCE
    }
}

enum class OverlayAnchor(val title: String, val x: Float, val y: Float) {
    TOP("أعلى", 0f, -0.78f),
    CENTER("وسط", 0f, 0f),
    BOTTOM("أسفل", 0f, 0.78f)
}


enum class OverlayColor(val id: String, val title: String, val argb: Int) {
    WHITE("white", "أبيض", 0xFFFFFFFF.toInt()),
    CYAN("cyan", "سماوي", 0xFF22D3EE.toInt()),
    YELLOW("yellow", "أصفر", 0xFFFFD166.toInt()),
    PINK("pink", "وردي", 0xFFFF4FD8.toInt()),
    RED("red", "أحمر", 0xFFFF5555.toInt()),
    GREEN("green", "أخضر", 0xFF74F09A.toInt());

    companion object {
        fun fromId(id: String): OverlayColor = entries.firstOrNull { it.id == id } ?: WHITE
    }
}
