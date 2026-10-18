package com.vizoedit.studio.settings

/** Output profile validation and a transparent file-size estimate (not a guaranteed size). */
data class ExportProfile(
    val framesPerSecond: Int = 30,
    val videoBitrateBitsPerSecond: Int = 8_000_000,
    val audioBitrateBitsPerSecond: Int = 192_000
) {
    fun validate(): ExportProfile {
        require(framesPerSecond in EditorSettings.ALLOWED_FRAME_RATES) { "Unsupported frame rate" }
        require(videoBitrateBitsPerSecond in 250_000..100_000_000) { "Video bitrate is out of range" }
        require(audioBitrateBitsPerSecond in 32_000..512_000) { "Audio bitrate is out of range" }
        return this
    }

    /** Decimal megabytes based on nominal combined bitrates; actual MP4 size can differ. */
    fun estimatedMegabytes(durationMs: Long): Double {
        validate()
        require(durationMs > 0L) { "Duration must be positive" }
        return (videoBitrateBitsPerSecond.toLong() + audioBitrateBitsPerSecond) * durationMs / 8_000_000_000.0
    }
}
