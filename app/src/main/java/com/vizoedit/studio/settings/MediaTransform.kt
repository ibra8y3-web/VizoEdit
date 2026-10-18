package com.vizoedit.studio.settings

/** Geometric transform settings. Normalized crop edges are in 0..1 of the decoded source frame. */
data class NormalizedCrop(val left: Float = 0f, val top: Float = 0f, val right: Float = 1f, val bottom: Float = 1f) {
    fun validate(): NormalizedCrop {
        require(listOf(left, top, right, bottom).all { it.isFinite() && it in 0f..1f }) { "Crop edges must be normalized between 0 and 1" }
        require(right - left >= MIN_VISIBLE_FRACTION) { "Crop width is too small" }
        require(bottom - top >= MIN_VISIBLE_FRACTION) { "Crop height is too small" }
        return this
    }

    companion object { const val MIN_VISIBLE_FRACTION = 0.03f }
}

data class MediaTransform(
    val rotationDegrees: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val crop: NormalizedCrop = NormalizedCrop()
) {
    fun validate(): MediaTransform {
        require(rotationDegrees.isFinite() && rotationDegrees in -180f..180f) { "Rotation must be between -180 and 180 degrees" }
        require(scaleX.isFinite() && scaleX in 0.25f..4f) { "Horizontal scale must be between 0.25 and 4" }
        require(scaleY.isFinite() && scaleY in 0.25f..4f) { "Vertical scale must be between 0.25 and 4" }
        crop.validate()
        return this
    }
}
