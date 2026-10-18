package com.vizoedit.studio.timeline

/** A source-backed timeline clip. Times are in milliseconds and relative to the source. */
data class TimelineClip(
    val id: String,
    val sourceUri: String,
    val sourceDurationMs: Long,
    val sourceStartMs: Long = 0L,
    val sourceEndMs: Long = sourceDurationMs,
    val timelineStartMs: Long = 0L,
    val track: Int = 0,
    val muted: Boolean = false,
    val volume: Float = 1f
) {
    val durationMs: Long get() = sourceEndMs - sourceStartMs
    val timelineEndMs: Long get() = timelineStartMs + durationMs

    fun validate(): TimelineClip {
        require(id.isNotBlank()) { "Clip id must not be blank" }
        require(sourceUri.isNotBlank()) { "Source URI must not be blank" }
        require(sourceDurationMs > 0L) { "Source duration must be positive" }
        require(sourceStartMs >= 0L && sourceEndMs <= sourceDurationMs) { "Clip range is outside source duration" }
        require(sourceEndMs > sourceStartMs) { "Clip duration must be positive" }
        require(timelineStartMs >= 0L) { "Timeline start must not be negative" }
        require(track >= 0) { "Track index must not be negative" }
        require(volume.isFinite() && volume in 0f..2f) { "Volume must be between 0 and 2" }
        return this
    }
}

/** Pure timeline operations. These model edits only; media rendering is performed by the export engine. */
object TimelineEditor {
    fun normalize(clips: List<TimelineClip>): List<TimelineClip> {
        clips.forEach(TimelineClip::validate)
        require(clips.map { it.id }.distinct().size == clips.size) { "Clip ids must be unique" }
        return clips.sortedWith(compareBy<TimelineClip> { it.track }.thenBy { it.timelineStartMs }.thenBy { it.id })
    }

    fun split(clip: TimelineClip, sourceSplitMs: Long, rightId: String): Pair<TimelineClip, TimelineClip> {
        clip.validate()
        require(rightId.isNotBlank() && rightId != clip.id) { "Right clip id must be unique and nonblank" }
        require(sourceSplitMs > clip.sourceStartMs && sourceSplitMs < clip.sourceEndMs) { "Split must be inside the clip" }
        val left = clip.copy(sourceEndMs = sourceSplitMs).validate()
        val right = clip.copy(
            id = rightId,
            sourceStartMs = sourceSplitMs,
            timelineStartMs = clip.timelineStartMs + (sourceSplitMs - clip.sourceStartMs)
        ).validate()
        return left to right
    }

    fun move(clip: TimelineClip, newTimelineStartMs: Long, newTrack: Int = clip.track): TimelineClip =
        clip.copy(timelineStartMs = newTimelineStartMs, track = newTrack).validate()

    fun delete(clips: List<TimelineClip>, id: String): List<TimelineClip> {
        require(clips.any { it.id == id }) { "Clip not found: $id" }
        return normalize(clips.filterNot { it.id == id })
    }
}
