package com.vizoedit.studio.subtitles
import org.junit.Assert.*
import org.junit.Test
class SrtCodecTest {
 @Test fun parsesAndWritesSrt() { val cues=SrtCodec.parse("1\n00:00:01,000 --> 00:00:02,500\nHello\nWorld"); assertEquals(1000L,cues.single().startMs); assertTrue(SrtCodec.write(cues).contains("Hello\nWorld")) }
 @Test(expected=IllegalArgumentException::class) fun rejectsInvalidRange() { SrtCodec.parse("1\n00:00:02,000 --> 00:00:01,000\nBad") }
}
