package com.vizoedit.studio.settings

/** Scalar animation track with deterministic interpolation. Times are milliseconds. */
data class ScalarKeyframe(val timeMs: Long, val value: Float)

enum class Interpolation { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }

class KeyframeTrack(keyframes: List<ScalarKeyframe>, val interpolation: Interpolation = Interpolation.LINEAR) {
    val keyframes: List<ScalarKeyframe> = keyframes.sortedBy { it.timeMs }

    init {
        require(this.keyframes.size >= 2) { "At least two keyframes are required" }
        require(this.keyframes.all { it.timeMs >= 0L && it.value.isFinite() }) { "Keyframes must have non-negative times and finite values" }
        require(this.keyframes.map { it.timeMs }.distinct().size == this.keyframes.size) { "Keyframe times must be unique" }
    }

    fun valueAt(timeMs: Long): Float {
        if (timeMs <= keyframes.first().timeMs) return keyframes.first().value
        if (timeMs >= keyframes.last().timeMs) return keyframes.last().value
        val rightIndex = keyframes.indexOfFirst { it.timeMs >= timeMs }
        val left = keyframes[rightIndex - 1]
        val right = keyframes[rightIndex]
        val raw = (timeMs - left.timeMs).toFloat() / (right.timeMs - left.timeMs).toFloat()
        val t = when (interpolation) {
            Interpolation.LINEAR -> raw
            Interpolation.EASE_IN -> raw * raw
            Interpolation.EASE_OUT -> 1f - (1f - raw) * (1f - raw)
            Interpolation.EASE_IN_OUT -> if (raw < 0.5f) 2f * raw * raw else 1f - ((-2f * raw + 2f).let { it * it } / 2f)
        }
        return left.value + (right.value - left.value) * t
    }
}
