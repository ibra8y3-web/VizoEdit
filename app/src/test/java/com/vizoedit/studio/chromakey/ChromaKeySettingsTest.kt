package com.vizoedit.studio.chromakey

import org.junit.Assert.assertSame
import org.junit.Test

class ChromaKeySettingsTest {
    @Test fun acceptsNormalizedSettings() { val value = ChromaKeySettings().validate(); assertSame(value, value) }
    @Test(expected = IllegalArgumentException::class) fun rejectsOutOfRangeSimilarity() {
        ChromaKeySettings(similarity = 1.5f).validate()
    }
}
