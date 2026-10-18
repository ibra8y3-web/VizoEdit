package com.vizoedit.studio.transitions
import org.junit.Assert.*
import org.junit.Test
class TransitionSettingsTest { @Test fun acceptsCommonTransition() { assertEquals(500L,TransitionSettings(TransitionType.FADE).validate().durationMs) }; @Test(expected=IllegalArgumentException::class) fun rejectsTooLongTransition() { TransitionSettings(TransitionType.ZOOM,20000).validate() } }
