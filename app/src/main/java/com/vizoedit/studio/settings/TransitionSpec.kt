package com.vizoedit.studio.settings

/** Declarative transition timing model. Export transitions require adjacent clips in a multi-clip composition. */
data class TransitionSpec(
    val type: TransitionType,
    val durationMs: Long = 500L,
    val startAtTimelineMs: Long = 0L
) {
    fun validate(projectDurationMs: Long): TransitionSpec {
        require(projectDurationMs > 0L) { "Project duration must be positive" }
        require(durationMs in 100L..5_000L) { "Transition duration must be 100–5000 ms" }
        require(startAtTimelineMs >= 0L && startAtTimelineMs + durationMs <= projectDurationMs) {
            "Transition lies outside project duration"
        }
        return this
    }
}

enum class TransitionType(val title: String) {
    FADE("تلاشي"), FLASH("وميض"), ZOOM("تكبير"), SLIDE_LEFT("انزلاق لليسار"),
    SLIDE_UP("انزلاق للأعلى"), WIPE("مسح"), BLUR("ضبابية"), DISSOLVE("ذوبان"),
    GLITCH("تشويش"), SPIN("دوران")
}
