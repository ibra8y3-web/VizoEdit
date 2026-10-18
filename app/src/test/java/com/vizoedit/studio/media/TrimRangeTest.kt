package com.vizoedit.studio.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrimRangeTest {
    @Test fun startIsClampedBeforeMinimumLength() {
        assertEquals(TrimRange(9_500L, 10_000L), TrimRange(0L, 10_000L).withStart(99_000L, 10_000L))
    }

    @Test fun endIsClampedToDuration() {
        assertEquals(TrimRange(2_000L, 10_000L), TrimRange(2_000L, 8_000L).withEnd(99_000L, 10_000L))
    }

    @Test fun invalidRangesAreRejected() {
        var failed = false
        try { TrimRange(4_000L, 4_100L).validate(10_000L) } catch (_: IllegalArgumentException) { failed = true }
        assertTrue(failed)
    }

    @Test fun historySupportsUndoRedoAndIgnoresDuplicateStates() {
        val history = TrimHistory()
        val a = TrimRange(0L, 10_000L)
        val b = TrimRange(1_000L, 10_000L)
        val c = TrimRange(1_000L, 8_000L)
        history.reset(a)
        history.record(a)
        history.record(b)
        history.record(c)
        assertTrue(history.canUndo)
        assertEquals(b, history.undo())
        assertEquals(c, history.redo())
        assertFalse(history.canRedo)
        assertNull(history.redo())
    }
    @Test fun newEditAfterUndoDropsRedoBranch() {
        val history = TrimHistory()
        val a = TrimRange(0L, 10_000L)
        val b = TrimRange(1_000L, 10_000L)
        val c = TrimRange(2_000L, 10_000L)
        val d = TrimRange(2_000L, 9_000L)
        history.reset(a)
        history.record(b)
        history.record(c)
        assertEquals(b, history.undo())
        history.record(d)
        assertFalse(history.canRedo)
        assertEquals(b, history.undo())
    }

}
