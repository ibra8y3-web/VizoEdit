package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorLookPresetTest {
    @Test fun normalPresetDoesNotChangeColorValues() {
        val base = EditorSettings(brightness = 0.1f, contrast = -0.1f, saturation = 12f)
        val applied = ColorLookPreset.NORMAL.applyTo(base).validate()
        assertEquals(base.brightness, applied.brightness, 0.0001f)
        assertEquals(base.contrast, applied.contrast, 0.0001f)
        assertEquals(base.saturation, applied.saturation, 0.0001f)
    }

    @Test fun monochromeReducesSaturationToMinimumForDefaultSettings() {
        val applied = ColorLookPreset.MONOCHROME.applyTo(EditorSettings()).validate()
        assertEquals(-100f, applied.saturation, 0.0001f)
    }

    @Test fun vividPresetChangesActualGradeParameters() {
        val base = EditorSettings()
        val applied = ColorLookPreset.VIVID.applyTo(base).validate()
        assertTrue(base.contrast != applied.contrast)
        assertTrue(base.saturation != applied.saturation)
    }

    @Test fun unknownPresetFallsBackToNormal() {
        assertEquals(ColorLookPreset.NORMAL, ColorLookPreset.fromId("not-a-real-look"))
    }
}
