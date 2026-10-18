package com.vizoedit.studio.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaTransformTest {
    @Test fun fullFrameCropIsValid() { assertEquals(1f, NormalizedCrop().validate().right, 0.0001f) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvertedCrop() { NormalizedCrop(left = 0.8f, right = 0.2f).validate() }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsScaleOutsideRange() { MediaTransform(scaleX = 5f).validate() }
}
