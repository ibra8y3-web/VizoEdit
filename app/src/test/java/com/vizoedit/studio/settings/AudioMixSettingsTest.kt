package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioMixSettingsTest {
    @Test fun defaultMixIsValid() {
        val settings = AudioMixSettings().validate(sourceDurationMs = 60_000, musicDurationMs = 20_000)
        assertEquals(1f, settings.sourceVolume)
    }

    @Test(expected = IllegalArgumentException::class)
    fun fadesCannotExceedDuration() {
        AudioMixSettings(fadeInMs = 40_000, fadeOutMs = 30_000).validate(60_000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun volumeCannotBeOutsideSupportedRange() {
        AudioMixSettings(musicVolume = 3f).validate(60_000)
    }
}
