package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class EditorSettingsTest {
    @Test fun defaultsAreValidAndNoOpSafe() {
        val settings = EditorSettings().validate()
        assertEquals(0f, settings.rotationDegrees)
        assertEquals(1f, settings.zoom)
        assertEquals(OutputCanvas.SOURCE.id, settings.outputCanvasId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeRotation() { EditorSettings(rotationDegrees = 181f).validate() }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidZoom() { EditorSettings(zoom = 0.5f).validate() }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownCanvas() { EditorSettings(outputCanvasId = "made-up").validate() }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnsupportedFps() { EditorSettings(outputFrameRate = 29).validate() }

    @Test fun outputCanvasFromIdFallsBackToSource() {
        assertSame(OutputCanvas.SOURCE, OutputCanvas.fromId("not-an-id"))
        assertEquals(1080, OutputCanvas.PORTRAIT_9_16.width)
        assertEquals(1920, OutputCanvas.PORTRAIT_9_16.height)
    }
}
