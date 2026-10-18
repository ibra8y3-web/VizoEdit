package com.vizoedit.studio.tools

enum class MaskShape { RECTANGLE, CIRCLE, LINEAR, CUSTOM }

data class MaskSpec(
    val shape: MaskShape,
    val centerX: Float = 0.5f,
    val centerY: Float = 0.5f,
    val width: Float = 1f,
    val height: Float = 1f,
    val feather: Float = 0f,
    val inverted: Boolean = false,
    val rotationDegrees: Float = 0f
) {
    fun validate(): MaskSpec {
        require(centerX.isFinite() && centerX in 0f..1f && centerY.isFinite() && centerY in 0f..1f)
        require(width.isFinite() && width in 0.001f..2f && height.isFinite() && height in 0.001f..2f)
        require(feather.isFinite() && feather in 0f..1f)
        require(rotationDegrees.isFinite() && rotationDegrees in -360f..360f)
        return this
    }
}
