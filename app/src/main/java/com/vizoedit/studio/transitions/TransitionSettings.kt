package com.vizoedit.studio.transitions

enum class TransitionType { FADE, DISSOLVE, CROSSFADE, SLIDE, PUSH, WIPE, ZOOM, SPIN, BLUR, GLITCH, FLASH, CIRCLE, SPLIT, ROTATE_3D }
enum class TransitionDirection { LEFT, RIGHT, UP, DOWN, IN, OUT }

data class TransitionSettings(val type: TransitionType, val durationMs: Long = 500, val direction: TransitionDirection = TransitionDirection.LEFT, val easing: Easing = Easing.EASE_IN_OUT) {
    fun validate(): TransitionSettings { require(durationMs in 1..10_000) { "Transition duration must be 1..10000ms" }; return this }
}
enum class Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT }
