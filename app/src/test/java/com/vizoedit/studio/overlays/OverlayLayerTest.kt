package com.vizoedit.studio.overlays
import org.junit.Assert.*
import org.junit.Test
class OverlayLayerTest {
 @Test fun validatesOverlay() { assertEquals(0.5f, OverlayLayer("t", OverlayKind.TEXT,0,1000,content="Hi").validate().x) }
 @Test(expected=IllegalArgumentException::class) fun rejectsOpacityOutsideRange() { OverlayLayer("t",OverlayKind.TEXT,0,1000,opacity=1.1f,content="Hi").validate() }
}
