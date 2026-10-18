package com.vizoedit.studio.settings

/** Real color-grade recipes applied by Media3EffectFactory during export. */
enum class ColorLookPreset(
    val id: String,
    val title: String,
    private val red: Float = 1f,
    private val green: Float = 1f,
    private val blue: Float = 1f,
    private val brightnessDelta: Float = 0f,
    private val contrastDelta: Float = 0f,
    private val hueDelta: Float = 0f,
    private val saturationDelta: Float = 0f,
    private val lightnessDelta: Float = 0f
) {
    NORMAL("normal", "أصلي"),
    VIVID("vivid", "حيوي", brightnessDelta = 0.04f, contrastDelta = 0.12f, saturationDelta = 22f),
    CINEMATIC("cinematic", "سينمائي", brightnessDelta = -0.02f, contrastDelta = 0.18f, saturationDelta = -8f, lightnessDelta = -3f),
    WARM("warm", "دافئ", red = 1.12f, green = 1.03f, blue = 0.88f, hueDelta = -6f, saturationDelta = 7f),
    COOL("cool", "بارد", red = 0.90f, green = 1.01f, blue = 1.14f, hueDelta = 8f, saturationDelta = -5f),
    MONOCHROME("mono", "أبيض وأسود", saturationDelta = -100f, contrastDelta = 0.08f),
    FADED("faded", "باهت", brightnessDelta = 0.05f, contrastDelta = -0.18f, saturationDelta = -14f, lightnessDelta = 5f),
    VINTAGE("vintage", "قديم", red = 1.10f, green = 0.98f, blue = 0.82f, hueDelta = -12f, saturationDelta = -18f, lightnessDelta = 3f),
    SEPIA("sepia", "سيبيا", red = 1.17f, green = 1.02f, blue = 0.78f, saturationDelta = -28f, lightnessDelta = 2f),
    NIGHT("night", "ليلي", brightnessDelta = -0.12f, contrastDelta = 0.16f, saturationDelta = -16f, lightnessDelta = -7f),
    SUNSET("sunset", "غروب", red = 1.14f, green = 0.98f, blue = 0.88f, hueDelta = -10f, saturationDelta = 18f, lightnessDelta = 2f);

    fun applyTo(settings: EditorSettings): EditorSettings = settings.copy(
        redScale = (settings.redScale * red).coerceIn(0.5f, 1.5f),
        greenScale = (settings.greenScale * green).coerceIn(0.5f, 1.5f),
        blueScale = (settings.blueScale * blue).coerceIn(0.5f, 1.5f),
        brightness = (settings.brightness + brightnessDelta).coerceIn(-1f, 1f),
        contrast = (settings.contrast + contrastDelta).coerceIn(-1f, 1f),
        hueDegrees = (settings.hueDegrees + hueDelta).coerceIn(-180f, 180f),
        saturation = (settings.saturation + saturationDelta).coerceIn(-100f, 100f),
        lightness = (settings.lightness + lightnessDelta).coerceIn(-100f, 100f),
        lookPresetId = NORMAL.id
    )

    companion object {
        fun fromId(id: String): ColorLookPreset = entries.firstOrNull { it.id == id } ?: NORMAL
    }
}
