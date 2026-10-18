package com.vizoedit.studio.settings

/** Output canvas presets. The export UI must apply these dimensions to the actual export composition. */
enum class CanvasPreset(val label: String, val width: Int, val height: Int) {
    LANDSCAPE_16_9("16:9", 1920, 1080),
    PORTRAIT_9_16("9:16", 1080, 1920),
    SQUARE_1_1("1:1", 1080, 1080),
    PORTRAIT_4_5("4:5", 1080, 1350),
    CLASSIC_4_3("4:3", 1440, 1080),
    CINEMA_21_9("21:9", 2520, 1080);

    val ratio: Float get() = width.toFloat() / height.toFloat()
}

data class AudioMixSettings(
    val sourceVolume: Float = 1f,
    val musicVolume: Float = 1f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val muteSource: Boolean = false,
    val loopMusic: Boolean = false
) {
    fun validate(sourceDurationMs: Long, musicDurationMs: Long? = null): AudioMixSettings {
        require(sourceDurationMs > 0L) { "Source duration must be positive" }
        require(sourceVolume.isFinite() && sourceVolume in 0f..2f) { "Source volume must be between 0 and 2" }
        require(musicVolume.isFinite() && musicVolume in 0f..2f) { "Music volume must be between 0 and 2" }
        require(fadeInMs >= 0L && fadeOutMs >= 0L) { "Fade durations cannot be negative" }
        require(fadeInMs + fadeOutMs <= sourceDurationMs) { "Audio fades exceed source duration" }
        if (musicDurationMs != null) require(musicDurationMs > 0L) { "Music duration must be positive" }
        return this
    }
}
