package com.vizoedit.studio.tools
import org.junit.Assert.*
import org.junit.Test
class MaskSpecTest { @Test fun acceptsDefaultMask() { assertEquals(MaskShape.RECTANGLE,MaskSpec(MaskShape.RECTANGLE).validate().shape) }; @Test(expected=IllegalArgumentException::class) fun rejectsInvalidFeather() { MaskSpec(MaskShape.CIRCLE,feather=2f).validate() } }
