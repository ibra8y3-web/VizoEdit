package com.vizoedit.studio.subtitles

data class SubtitleCue(val startMs: Long, val endMs: Long, val text: String) {
    fun validate(): SubtitleCue {
        require(startMs >= 0 && endMs > startMs) { "Subtitle times must be valid and increasing" }
        require(text.isNotBlank()) { "Subtitle text cannot be blank" }
        return this
    }
}

/** Strict, dependency-free SRT import/export. Supports comma or dot millisecond separators. */
object SrtCodec {
    private val time = Regex("(\\d{2,}):(\\d{2}):(\\d{2})[,.](\\d{3})")
    fun parse(srt: String): List<SubtitleCue> {
        val blocks = srt.trim().replace("\r\n", "\n").split(Regex("""\n\s*\n""")).filter { it.isNotBlank() }
        val cues = blocks.map { block ->
            val lines = block.lines()
            require(lines.size >= 3) { "Invalid SRT block" }
            val timeLine = lines.firstOrNull { it.contains(" --> ") } ?: error("Missing SRT time range")
            val parts = timeLine.split(" --> ")
            require(parts.size == 2) { "Invalid SRT time range" }
            fun millis(value: String): Long {
                val m = time.matchEntire(value.trim()) ?: error("Invalid SRT timestamp: $value")
                return m.groupValues[1].toLong() * 3_600_000 + m.groupValues[2].toLong() * 60_000 + m.groupValues[3].toLong() * 1_000 + m.groupValues[4].toLong()
            }
            val index = lines.indexOf(timeLine)
            SubtitleCue(millis(parts[0]), millis(parts[1]), lines.drop(index + 1).joinToString("\n")).validate()
        }
        require(cues.zipWithNext().all { (a, b) -> b.startMs >= a.startMs }) { "Subtitle cues must be ordered" }
        return cues
    }
    fun write(cues: List<SubtitleCue>): String {
        require(cues.zipWithNext().all { (a, b) -> b.startMs >= a.startMs }) { "Subtitle cues must be ordered" }
        fun stamp(ms: Long): String {
            require(ms >= 0)
            val h = ms / 3_600_000; val m = ms / 60_000 % 60; val s = ms / 1_000 % 60; val milli = ms % 1_000
            return "%02d:%02d:%02d,%03d".format(h, m, s, milli)
        }
        return cues.mapIndexed { i, cue -> cue.validate(); "${i + 1}\n${stamp(cue.startMs)} --> ${stamp(cue.endMs)}\n${cue.text}" }.joinToString("\n\n")
    }
}
