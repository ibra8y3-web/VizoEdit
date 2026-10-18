package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class OverlaySettingsTest {
    @Test fun defaultOverlaySettingsAreValid() {
        val value = EditorSettings().validate()
        assertEquals(1f, value.overlayOpacity, 0.0001f)
        assertEquals(0f, value.overlayRotationDegrees, 0.0001f)
        assertSame(OverlayColor.WHITE, OverlayColor.fromId(value.overlayColorId))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOpacityAboveOne() { EditorSettings(overlayOpacity = 1.1f).validate() }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRotationOutsideRange() { EditorSettings(overlayRotationDegrees = 181f).validate() }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownOverlayColor() { EditorSettings(overlayColorId = "invisible").validate() }
}
