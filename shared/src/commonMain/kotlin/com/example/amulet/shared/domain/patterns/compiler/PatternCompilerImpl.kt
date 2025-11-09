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
import com.example.amulet.shared.domain.patterns.model.PatternSpec
import com.example.amulet.shared.domain.patterns.model.SequenceStep

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
            }
        }
        
        return AmuletCommandPlan(
            commands = commands,
            estimatedDurationMs = totalDuration
        )
    }
}
