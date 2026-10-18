package com.vizoedit.studio.chromakey

/** Parameters for a chroma-key processor. Values are normalized to 0..1. */
data class ChromaKeySettings(
    val keyColorArgb: Int = 0xFF00FF00.toInt(),
    val similarity: Float = 0.12f,
    val smoothness: Float = 0.08f,
    val spillSuppression: Float = 0.2f,
    val edgeFeather: Float = 0.02f
) {
    fun validate(): ChromaKeySettings {
        require(similarity.isFinite() && similarity in 0f..1f)
        require(smoothness.isFinite() && smoothness in 0f..1f)
        require(spillSuppression.isFinite() && spillSuppression in 0f..1f)
        require(edgeFeather.isFinite() && edgeFeather in 0f..1f)
        return this
    }
}
