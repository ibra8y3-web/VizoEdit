package com.vizoedit.studio.media

/** Pure trim-range logic shared by the editor UI and unit tests. Times are milliseconds. */
data class TrimRange(val startMs: Long, val endMs: Long) {
    fun validate(durationMs: Long, minLengthMs: Long = DEFAULT_MIN_LENGTH_MS): TrimRange {
        require(durationMs > 0L) { "Video duration must be positive" }
        require(startMs >= 0L) { "Trim start cannot be negative" }
        require(endMs <= durationMs) { "Trim end exceeds video duration" }
        require(endMs - startMs >= minLengthMs) { "Trim range is too short" }
        return this
    }

    fun withStart(requestedMs: Long, durationMs: Long, minLengthMs: Long = DEFAULT_MIN_LENGTH_MS): TrimRange {
        val safeEnd = endMs.coerceIn(minLengthMs, durationMs)
        val nextStart = requestedMs.coerceIn(0L, (safeEnd - minLengthMs).coerceAtLeast(0L))
        return TrimRange(nextStart, safeEnd)
    }

    fun withEnd(requestedMs: Long, durationMs: Long, minLengthMs: Long = DEFAULT_MIN_LENGTH_MS): TrimRange {
        val safeStart = startMs.coerceIn(0L, (durationMs - minLengthMs).coerceAtLeast(0L))
        val nextEnd = requestedMs.coerceIn(safeStart + minLengthMs, durationMs)
        return TrimRange(safeStart, nextEnd)
    }

    companion object {
        const val DEFAULT_MIN_LENGTH_MS = 500L
        fun full(durationMs: Long): TrimRange = TrimRange(0L, durationMs.coerceAtLeast(0L))
    }
}

/** Bounded undo/redo history for trim-range edits. Duplicate consecutive states are ignored. */
class TrimHistory(private val maxEntries: Int = 100) {
    private val entries = mutableListOf<TrimRange>()
    private var cursor = -1

    val canUndo: Boolean get() = cursor > 0
    val canRedo: Boolean get() = cursor >= 0 && cursor < entries.lastIndex

    fun reset(initial: TrimRange) {
        entries.clear()
        entries += initial
        cursor = 0
    }

    fun record(range: TrimRange) {
        if (cursor >= 0 && entries[cursor] == range) return
        while (entries.lastIndex > cursor) entries.removeAt(entries.lastIndex)
        entries += range
        if (entries.size > maxEntries.coerceAtLeast(2)) entries.removeAt(0)
        cursor = entries.lastIndex
    }

    fun undo(): TrimRange? {
        if (!canUndo) return null
        cursor -= 1
        return entries[cursor]
    }

    fun redo(): TrimRange? {
        if (!canRedo) return null
        cursor += 1
        return entries[cursor]
    }
}
