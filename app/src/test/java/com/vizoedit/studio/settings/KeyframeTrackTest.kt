package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyframeTrackTest {
    @Test fun linearInterpolationIsDeterministic() {
        val track = KeyframeTrack(listOf(ScalarKeyframe(100, 0f), ScalarKeyframe(300, 10f)))
        assertEquals(0f, track.valueAt(0), 0.001f)
        assertEquals(5f, track.valueAt(200), 0.001f)
        assertEquals(10f, track.valueAt(900), 0.001f)
    }

    @Test fun easeInStartsMoreSlowlyThanLinear() {
        val keys = listOf(ScalarKeyframe(0, 0f), ScalarKeyframe(100, 1f))
        assertTrue(KeyframeTrack(keys, Interpolation.EASE_IN).valueAt(25) < KeyframeTrack(keys).valueAt(25))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateTimestamps() {
        KeyframeTrack(listOf(ScalarKeyframe(0, 0f), ScalarKeyframe(0, 1f)))
    }
}
