package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.patterns.model.Pattern
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.PatternTimeline
import com.example.amulet.shared.domain.patterns.model.TimelineClip
import com.example.amulet.shared.domain.patterns.model.TimelineTrack
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

private const val MIN_SEGMENT_DURATION_MS = 300

class SlicePatternIntoSegmentsUseCase {

    operator fun invoke(
        basePattern: Pattern,
        markersMs: List<Int>
    ): AppResult<List<Pattern>> {
        val timeline = basePattern.spec.timeline
        val totalDuration = timeline.durationMs

        if (markersMs.isEmpty()) {
            return Err(AppError.Validation(mapOf("markers" to "Markers list must not be empty")))
        }

        val raw = markersMs.sorted()
        if (raw.first() < 0 || raw.last() > totalDuration) {
            return Err(AppError.Validation(mapOf("markers" to "Markers must be within [0, $totalDuration]")))
        }

        val distinct = raw.distinct()
        val normalized = buildList {
            if (distinct.first() != 0) add(0)
            addAll(distinct)
            if (last() != totalDuration) add(totalDuration)
        }

        if (normalized.size < 2) {
            return Err(AppError.Validation(mapOf("markers" to "At least two markers (start/end) are required")))
        }

        for (i in 0 until normalized.lastIndex) {
            if (normalized[i + 1] <= normalized[i]) {
                return Err(AppError.Validation(mapOf("markers" to "Markers must be strictly increasing")))
            }
        }

        val segments = mutableListOf<Pattern>()

        for (index in 0 until normalized.lastIndex) {
            val segStart = normalized[index]
            val segEnd = normalized[index + 1]
            val segDuration = segEnd - segStart

            if (segDuration < MIN_SEGMENT_DURATION_MS) {
                return Err(
                    AppError.Validation(
                        mapOf("markers" to "Segment duration must be >= ${MIN_SEGMENT_DURATION_MS} ms")
                    )
                )
            }

            val newTracks = timeline.tracks.map { track ->
                val newClips = mutableListOf<TimelineClip>()
                for (clip in track.clips) {
                    val clipStart = clip.startMs
                    val clipEnd = clip.startMs + clip.durationMs
                    val interStart = maxOf(segStart, clipStart)
                    val interEnd = minOf(segEnd, clipEnd)
                    if (interEnd <= interStart) continue

                    val newStart = interStart - segStart
                    val newDuration = interEnd - interStart

                    val cutAtStart = interStart - clipStart
                    val cutAtEnd = clipEnd - interEnd

                    val newFadeIn = (clip.fadeInMs - cutAtStart).coerceAtLeast(0).coerceAtMost(newDuration)
                    val newFadeOut = (clip.fadeOutMs - cutAtEnd).coerceAtLeast(0).coerceAtMost(newDuration)

                    newClips += clip.copy(
                        startMs = newStart,
                        durationMs = newDuration,
                        fadeInMs = newFadeIn,
                        fadeOutMs = newFadeOut,
                    )
                }
                TimelineTrack(
                    target = track.target,
                    priority = track.priority,
                    mixMode = track.mixMode,
                    clips = newClips
                )
            }

            val segmentTimeline = PatternTimeline(
                durationMs = segDuration,
                tracks = newTracks
            )

            val segmentSpec = PatternSpec(
                type = basePattern.spec.type,
                hardwareVersion = basePattern.spec.hardwareVersion,
                durationMs = segDuration,
                loop = false,
                timeline = segmentTimeline
            )

            val segmentId = PatternId("${basePattern.id.value}_seg_$index")
            val segmentTags = (basePattern.tags + "internal_step").distinct()

            val segment = Pattern(
                id = segmentId,
                version = 1,
                ownerId = basePattern.ownerId,
                kind = basePattern.kind,
                spec = segmentSpec,
                public = basePattern.public,
                reviewStatus = basePattern.reviewStatus,
                hardwareVersion = basePattern.hardwareVersion,
                title = basePattern.title,
                description = basePattern.description,
                tags = segmentTags,
                usageCount = 0,
                sharedWith = emptyList(),
                createdAt = basePattern.createdAt,
                updatedAt = basePattern.updatedAt,
                parentPatternId = basePattern.id,
                segmentIndex = index,
                segmentStartMs = segStart,
                segmentEndMs = segEnd,
            )

            segments += segment
        }

        return Ok(segments)
    }
}
