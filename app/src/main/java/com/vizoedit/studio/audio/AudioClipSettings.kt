package com.vizoedit.studio.audio

/** Validated, source-backed audio clip parameters. Processing must be applied by the renderer. */
data class AudioClipSettings(
    val sourceUri: String,
    val timelineStartMs: Long = 0,
    val sourceStartMs: Long = 0,
    val sourceEndMs: Long,
    val gain: Float = 1f,
    val fadeInMs: Long = 0,
    val fadeOutMs: Long = 0,
    val muted: Boolean = false,
    val loop: Boolean = false,
    val reverse: Boolean = false,
    val pitchSemitones: Float = 0f
) {
    val durationMs: Long get() = sourceEndMs - sourceStartMs
    fun validate(): AudioClipSettings {
        require(sourceUri.isNotBlank())
        require(timelineStartMs >= 0 && sourceStartMs >= 0 && sourceEndMs > sourceStartMs)
        require(gain.isFinite() && gain in 0f..4f)
        require(fadeInMs >= 0 && fadeOutMs >= 0 && fadeInMs + fadeOutMs <= durationMs)
        require(pitchSemitones.isFinite() && pitchSemitones in -12f..12f)
        return this
    }
}
