package com.example.amulet.shared.domain.patterns.compiler

import com.example.amulet.shared.domain.devices.model.AmuletCommand
import com.example.amulet.shared.domain.devices.model.AmuletCommandPlan
import com.example.amulet.shared.domain.devices.model.ChaseDirection
import com.example.amulet.shared.domain.devices.model.Rgb
import com.example.amulet.shared.domain.patterns.model.PatternElementBreathing
import com.example.amulet.shared.domain.patterns.model.PatternElementChase
import com.example.amulet.shared.domain.patterns.model.PatternElementFill
import com.example.amulet.shared.domain.patterns.model.PatternElementProgress
import com.example.amulet.shared.domain.patterns.model.PatternElementPulse
import com.example.amulet.shared.domain.patterns.model.PatternElementSequence
import com.example.amulet.shared.domain.patterns.model.PatternElementSpinner
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.SequenceStep
import com.example.amulet.shared.domain.patterns.model.MixMode
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetLed
import com.example.amulet.shared.domain.patterns.model.TargetRing

/**
 * Реализация компилятора паттернов.
 * Преобразует элементы паттерна в команды для устройства.
 */
class PatternCompilerImpl : PatternCompiler {
    
    override fun compile(
        spec: PatternSpec,
        hardwareVersion: Int,
        firmwareVersion: String
    ): AmuletCommandPlan {
        val commands = mutableListOf<AmuletCommand>()
        var totalDuration = 0L
        
        for (element in spec.elements) {
            when (element) {
                is PatternElementBreathing -> {
                    // Команда BREATHING:color:durationMs
                    commands.add(AmuletCommand.Breathing(
                        color = Rgb.fromHex(element.color),
                        durationMs = element.durationMs
                    ))
                    totalDuration += element.durationMs
                }
                
                is PatternElementPulse -> {
                    // Команда PULSE:color:intervalMs:repeats
                    commands.add(AmuletCommand.Pulse(
                        color = Rgb.fromHex(element.color),
                        intervalMs = element.speed,
                        repeats = element.repeats
                    ))
                    totalDuration += (element.speed * element.repeats).toLong()
                }
                
                is PatternElementChase -> {
                    // Команда CHASE:color:direction:speedMs
                    val direction = when (element.direction) {
                        com.example.amulet.shared.domain.patterns.model.ChaseDirection.CLOCKWISE -> 
                            ChaseDirection.CLOCKWISE
                        com.example.amulet.shared.domain.patterns.model.ChaseDirection.COUNTER_CLOCKWISE -> 
                            ChaseDirection.COUNTER_CLOCKWISE
                    }
                    commands.add(AmuletCommand.Chase(
                        color = Rgb.fromHex(element.color),
                        direction = direction,
                        speedMs = element.speedMs
                    ))
                    // Длительность зависит от количества диодов и скорости
                    totalDuration += (element.speedMs * 8).toLong()
                }
                
                is PatternElementFill -> {
                    // Команда FILL:color:durationMs
                    commands.add(AmuletCommand.Fill(
                        color = Rgb.fromHex(element.color),
                        durationMs = element.durationMs
                    ))
                    totalDuration += element.durationMs
                }
                
                is PatternElementSpinner -> {
                    // Команда SPINNER:color1,color2:speedMs
                    commands.add(AmuletCommand.Spinner(
                        colors = element.colors.map { Rgb.fromHex(it) },
                        speedMs = element.speedMs
                    ))
                    // Длительность одного оборота
                    totalDuration += (element.speedMs * 8).toLong()
                }
                
                is PatternElementProgress -> {
                    // Команда PROGRESS:color:activeLeds
                    commands.add(AmuletCommand.Progress(
                        color = Rgb.fromHex(element.color),
                        activeLeds = element.activeLeds
                    ))
                    // Статичный элемент, длительность не добавляется
                }
                
                is PatternElementSequence -> {
                    // Специальная обработка последовательностей для "секретных кодов"
                    // Транслируется в SET_LED и DELAY команды
                    for (step in element.steps) {
                        when (step) {
                            is SequenceStep.LedAction -> {
                                // Команда SET_LED:index:color
                                commands.add(AmuletCommand.SetLed(
                                    index = step.ledIndex,
                                    color = Rgb.fromHex(step.color)
                                ))
                                // Добавляем задержку для длительности свечения
                                if (step.durationMs > 0) {
                                    commands.add(AmuletCommand.Delay(
                                        durationMs = step.durationMs
                                    ))
                                    totalDuration += step.durationMs
                                }
                                // Выключаем диод после задержки
                                commands.add(AmuletCommand.SetLed(
                                    index = step.ledIndex,
                                    color = Rgb(0, 0, 0)
                                ))
                            }
                            is SequenceStep.DelayAction -> {
                                // Команда DELAY:durationMs
                                commands.add(AmuletCommand.Delay(
                                    durationMs = step.durationMs
                                ))
                                totalDuration += step.durationMs
                            }
                        }
                    }
                }
                is PatternElementTimeline -> {
                    val (timelineCommands, timelineDuration) = compileTimeline(element)
                    commands.addAll(timelineCommands)
                    totalDuration += timelineDuration
                }
            }
        }
        
        return AmuletCommandPlan(
            commands = commands,
            estimatedDurationMs = totalDuration
        )
    }

    private fun compileTimeline(element: PatternElementTimeline): Pair<List<AmuletCommand>, Long> {
        val commands = mutableListOf<AmuletCommand>()
        val leds = 8
        val duration = element.durationMs
        val tick = element.tickMs.coerceAtLeast(1)
        var elapsed = 0
        var prevColors = List(leds) { Rgb(0, 0, 0) }

        while (elapsed < duration) {
            val ring = computeRingAtTime(element, elapsed)
            val changed = mutableListOf<Int>()
            for (i in 0 until leds) {
                if (ring[i] != prevColors[i]) changed.add(i)
            }

            if (changed.isNotEmpty()) {
                if (changed.size <= 2) {
                    changed.forEach { idx ->
                        commands.add(AmuletCommand.SetLed(index = idx, color = ring[idx]))
                    }
                } else {
                    commands.add(AmuletCommand.SetRing(colors = ring))
                }
                prevColors = ring
            }

            val step = minOf(tick, duration - elapsed)
            if (step > 0) {
                commands.add(AmuletCommand.Delay(durationMs = step))
            }
            elapsed += step
        }

        return commands to duration.toLong()
    }

    private fun computeRingAtTime(element: PatternElementTimeline, t: Int): List<Rgb> {
        val leds = 8
        val contributions = Array(leds) { mutableListOf<TrackContribution>() }

        element.tracks.forEach { track ->
            val color = trackColorAt(track, t)
            if (color != null) {
                when (val target = track.target) {
                    is TargetLed -> if (target.index in 0 until leds) {
                        contributions[target.index].add(TrackContribution(track.priority, track.mixMode, color))
                    }
                    is TargetGroup -> target.indices.forEach { idx ->
                        if (idx in 0 until leds) contributions[idx].add(TrackContribution(track.priority, track.mixMode, color))
                    }
                    is TargetRing -> (0 until leds).forEach { idx ->
                        contributions[idx].add(TrackContribution(track.priority, track.mixMode, color))
                    }
                }
            }
        }

        return contributions.map { list ->
            if (list.isEmpty()) Rgb(0, 0, 0) else mixContributions(list)
        }
    }

    private fun trackColorAt(track: com.example.amulet.shared.domain.patterns.model.TimelineTrack, t: Int): Rgb? {
        val clip = track.clips.firstOrNull { c -> t >= c.startMs && t < c.startMs + c.durationMs }
        if (clip != null) {
            val base = Rgb.fromHex(clip.color)
            val rel = (t - clip.startMs).coerceAtLeast(0)
            val fadeIn = if (clip.fadeInMs > 0) (rel.toFloat() / clip.fadeInMs).coerceIn(0f, 1f) else 1f
            val relOut = (clip.startMs + clip.durationMs - t).coerceAtLeast(0)
            val fadeOut = if (clip.fadeOutMs > 0) (relOut.toFloat() / clip.fadeOutMs).coerceIn(0f, 1f) else 1f
            val factor = minOf(fadeIn, fadeOut)
            return scaleRgb(base, factor)
        }
        return null
    }

    private data class TrackContribution(val priority: Int, val mixMode: MixMode, val color: Rgb)

    private fun mixContributions(items: List<TrackContribution>): Rgb {
        val sorted = items.sortedWith(compareBy({ it.priority }))
        var acc = Rgb(0, 0, 0)
        for (c in sorted) {
            acc = when (c.mixMode) {
                MixMode.OVERRIDE -> c.color
                MixMode.ADDITIVE -> addRgb(acc, c.color)
            }
        }
        return acc
    }

    private fun addRgb(a: Rgb, b: Rgb): Rgb = Rgb(
        red = (a.red + b.red).coerceAtMost(255),
        green = (a.green + b.green).coerceAtMost(255),
        blue = (a.blue + b.blue).coerceAtMost(255)
    )

    private fun scaleRgb(c: Rgb, factor: Float): Rgb = Rgb(
        red = (c.red * factor).toInt().coerceIn(0, 255),
        green = (c.green * factor).toInt().coerceIn(0, 255),
        blue = (c.blue * factor).toInt().coerceIn(0, 255)
    )
}
