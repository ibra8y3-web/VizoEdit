package com.vizoedit.studio.audio
import org.junit.Assert.*
import org.junit.Test
class AudioClipSettingsTest {
 @Test fun validClipAccepted() { assertEquals(1000, AudioClipSettings("content://audio", sourceEndMs=1000).validate().durationMs) }
 @Test(expected=IllegalArgumentException::class) fun fadesCannotExceedClip() { AudioClipSettings("x", sourceEndMs=1000, fadeInMs=800, fadeOutMs=300).validate() }
 @Test(expected=IllegalArgumentException::class) fun gainBounded() { AudioClipSettings("x", sourceEndMs=1000, gain=5f).validate() }
}
