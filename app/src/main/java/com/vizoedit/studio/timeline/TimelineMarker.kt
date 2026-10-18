package com.vizoedit.studio.timeline

/** User-created timeline marker. */
data class TimelineMarker(val id: String, val timeMs: Long, val label: String = "") {
    init {
        require(id.isNotBlank()) { "Marker id cannot be blank" }
        require(timeMs >= 0L) { "Marker time cannot be negative" }
    }
}

/** Finds the nearest snap target within tolerance, otherwise returns the requested time. */
fun snapTime(requestedMs: Long, snapTargetsMs: Collection<Long>, toleranceMs: Long): Long {
    require(requestedMs >= 0L && toleranceMs >= 0L)
    val target = snapTargetsMs.asSequence().filter { it >= 0L }
        .minByOrNull { kotlin.math.abs(it - requestedMs) }
    return if (target != null && kotlin.math.abs(target - requestedMs) <= toleranceMs) target else requestedMs
}
