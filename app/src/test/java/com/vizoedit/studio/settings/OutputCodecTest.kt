package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class OutputCodecTest {
    @Test fun h264IsDefaultAndH265IsDistinct() {
        assertEquals(OutputCodec.H264, OutputCodec.fromId(EditorSettings().outputCodecId))
        assertEquals("video/avc", OutputCodec.H264.mimeType)
        assertEquals("video/hevc", OutputCodec.H265.mimeType)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsUnknownCodec() {
        EditorSettings(outputCodecId = "not-supported").validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeVideoBitrate() {
        EditorSettings(outputVideoBitrateBitsPerSecond = 100_000).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsOutOfRangeAudioBitrate() {
        EditorSettings(outputAudioBitrateBitsPerSecond = 12_000).validate()
    }
}
