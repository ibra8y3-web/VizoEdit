package com.vizoedit.studio.motion

/** Piecewise-linear speed curve. Time is clip-local milliseconds; speed is a multiplier. */
data class SpeedPoint(val timeMs: Long, val speed: Float)

class SpeedCurve(points: List<SpeedPoint>) {
    val points: List<SpeedPoint> = points.sortedBy { it.timeMs }

    init {
        require(this.points.size >= 2) { "A speed curve requires at least two points" }
        require(this.points.all { it.timeMs >= 0L && it.speed.isFinite() && it.speed in 0.25f..4f }) {
            "Times must be non-negative and speed must be between 0.25x and 4x"
        }
        require(this.points.zipWithNext().all { (a, b) -> a.timeMs < b.timeMs }) {
            "Speed point times must be strictly increasing"
        }
    }

    fun speedAt(timeMs: Long): Float {
        if (timeMs <= points.first().timeMs) return points.first().speed
        if (timeMs >= points.last().timeMs) return points.last().speed
        val index = points.indexOfFirst { it.timeMs >= timeMs }
        val a = points[index - 1]
        val b = points[index]
        val t = (timeMs - a.timeMs).toFloat() / (b.timeMs - a.timeMs).toFloat()
        return a.speed + (b.speed - a.speed) * t
    }
}
