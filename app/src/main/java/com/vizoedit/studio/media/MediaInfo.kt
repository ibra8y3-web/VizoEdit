package com.vizoedit.studio.media

/** Metadata obtained from a real media probe; unknown values remain null rather than guessed. */
data class MediaInfo(
    val durationMs: Long?,
    val width: Int?,
    val height: Int?,
    val frameRate: Float?,
    val videoCodec: String?,
    val audioCodec: String?,
    val fileSizeBytes: Long?
) {
    fun validate(): MediaInfo {
        require(durationMs == null || durationMs >= 0L)
        require(width == null || width > 0)
        require(height == null || height > 0)
        require(frameRate == null || (frameRate.isFinite() && frameRate > 0f))
        require(fileSizeBytes == null || fileSizeBytes >= 0L)
        return this
    }
}
