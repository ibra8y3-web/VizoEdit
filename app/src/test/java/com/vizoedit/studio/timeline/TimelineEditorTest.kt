package com.vizoedit.studio.timeline

import org.junit.Assert.*
import org.junit.Test

class TimelineEditorTest {
    private fun clip() = TimelineClip("a", "content://video/1", 10_000, 1_000, 9_000, 500)

    @Test fun splitPreservesDurationAndTimelineContinuity() {
        val (left, right) = TimelineEditor.split(clip(), 4_000, "b")
        assertEquals(3_000L, left.durationMs)
        assertEquals(5_000L, right.durationMs)
        assertEquals(left.timelineEndMs, right.timelineStartMs)
    }

    @Test fun normalizeSortsByTrackThenStart() {
        val a = clip()
        val b = clip().copy(id = "b", timelineStartMs = 0, track = 1)
        assertEquals(listOf(a, b), TimelineEditor.normalize(listOf(b, a)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun splitAtBoundaryIsRejected() { TimelineEditor.split(clip(), 1_000, "b") }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateIdsAreRejected() { TimelineEditor.normalize(listOf(clip(), clip())) }
}
