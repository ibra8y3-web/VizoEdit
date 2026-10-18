package com.vizoedit.studio.overlays

enum class OverlayKind { TEXT, IMAGE, EMOJI, SHAPE, SUBTITLE, LOGO }
enum class BlendModeSpec { NORMAL, MULTIPLY, SCREEN, OVERLAY, ADD }

data class OverlayLayer(
    val id: String,
    val kind: OverlayKind,
    val startMs: Long,
    val endMs: Long,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val scale: Float = 1f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1f,
    val content: String = "",
    val blendMode: BlendModeSpec = BlendModeSpec.NORMAL
) {
    fun validate(): OverlayLayer {
        require(id.isNotBlank() && endMs > startMs && startMs >= 0)
        require(x.isFinite() && x in 0f..1f && y.isFinite() && y in 0f..1f)
        require(scale.isFinite() && scale in 0.01f..20f)
        require(rotationDegrees.isFinite() && rotationDegrees in -3600f..3600f)
        require(opacity.isFinite() && opacity in 0f..1f)
        if (kind != OverlayKind.SHAPE) require(content.isNotBlank())
        return this
    }
}
