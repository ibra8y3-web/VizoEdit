package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class ExportProfileTest {
    @Test fun computesNominalMegabytesFromBitrate() {
        val profile = ExportProfile(framesPerSecond = 30, videoBitrateBitsPerSecond = 8_000_000, audioBitrateBitsPerSecond = 192_000)
        assertEquals(8.192, profile.estimatedMegabytes(8_000), 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidBitrate() { ExportProfile(videoBitrateBitsPerSecond = 100).validate() }
}
