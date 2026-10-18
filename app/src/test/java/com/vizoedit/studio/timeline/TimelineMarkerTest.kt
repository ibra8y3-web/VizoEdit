package com.vizoedit.studio.timeline

import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineMarkerTest {
    @Test fun snapsToNearestTargetWithinTolerance() { assertEquals(100L, snapTime(96, listOf(50, 100, 150), 5)) }
    @Test fun leavesTimeAloneOutsideTolerance() { assertEquals(96L, snapTime(96, listOf(100), 3)) }
}
