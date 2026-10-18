package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class TransitionSpecTest {
    @Test fun validTransitionStaysWithinProject() {
        val spec = TransitionSpec(TransitionType.FADE, 400, 600).validate(1_000)
        assertEquals(400L, spec.durationMs)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsTransitionPastProjectEnd() { TransitionSpec(TransitionType.ZOOM, 600, 500).validate(1_000) }
}
