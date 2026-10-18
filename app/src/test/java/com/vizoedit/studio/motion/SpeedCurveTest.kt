package com.vizoedit.studio.motion

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedCurveTest {
    @Test fun interpolatesBetweenPoints() {
        val curve = SpeedCurve(listOf(SpeedPoint(0, 1f), SpeedPoint(1000, 3f)))
        assertEquals(2f, curve.speedAt(500), 0.0001f)
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsDuplicateTimes() {
        SpeedCurve(listOf(SpeedPoint(0, 1f), SpeedPoint(0, 2f)))
    }
}
