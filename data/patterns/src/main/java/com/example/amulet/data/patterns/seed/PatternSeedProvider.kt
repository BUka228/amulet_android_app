package com.example.amulet.data.patterns.seed

import com.example.amulet.shared.domain.patterns.builder.PresetPatterns
import com.example.amulet.shared.domain.patterns.builder.buildPattern
import com.example.amulet.shared.domain.patterns.model.ChaseDirection
import com.example.amulet.shared.domain.patterns.model.Easing
import com.example.amulet.shared.domain.patterns.model.MixMode
import com.example.amulet.shared.domain.patterns.model.PatternElementSequence
import com.example.amulet.shared.domain.patterns.model.PatternElementTimeline
import com.example.amulet.shared.domain.patterns.model.SequenceStep
import com.example.amulet.shared.domain.patterns.model.TargetGroup
import com.example.amulet.shared.domain.patterns.model.TargetLed
import com.example.amulet.shared.domain.patterns.model.TargetRing
import com.example.amulet.shared.domain.patterns.model.TimelineClip
import com.example.amulet.shared.domain.patterns.model.TimelineTrack

/**
 * Провайдер предустановленных паттернов (много пресетов для разных сценариев).
 * Включает таймлайны и последовательности.
 */
class PatternSeedProvider(
    private val hardwareVersion: Int = 100
) {
    fun provideAll(): List<PatternSeed> = buildList {
        // Базовые эмоции/состояния
        add(seed("preset_calm", "Спокойствие", "Мягкое дыхание для расслабления", listOf("расслабление", "медитация", "сон"), PresetPatterns.calm(hardwareVersion)))
        add(seed("preset_meditation", "Медитация", "Глубокое дыхание для медитации", listOf("медитация", "расслабление"), PresetPatterns.meditation(hardwareVersion)))
        add(seed("preset_energy", "Энергия", "Быстрые пульсации для бодрости", listOf("фокус", "тренировка"), PresetPatterns.energy(hardwareVersion)))
        add(seed("preset_focus", "Фокус", "Визуальный фокус/прогресс", listOf("фокус", "продуктивность"), PresetPatterns.focus(hardwareVersion)))
        add(seed("preset_joy", "Радость", "Вращающийся радостный свет", listOf("вечеринка", "радость"), PresetPatterns.joy(hardwareVersion)))
        add(seed("preset_love", "Любовь", "Розовое дыхание и пульс", listOf("любовь", "объятия"), PresetPatterns.love(hardwareVersion)))
        add(seed("preset_rainbow", "Радуга", "Цвета радуги по очереди", listOf("вечеринка", "дети"), PresetPatterns.rainbow(hardwareVersion)))
        add(seed("preset_party", "Вечеринка", "Быстрые разноцветные спиннеры", listOf("вечеринка"), PresetPatterns.party(hardwareVersion)))
        add(seed("preset_breathing_478", "Дыхание 4-7-8", "Визуализация дыхательной техники", listOf("дыхание", "практика"), PresetPatterns.breathing478(hardwareVersion)))

        // Секретные коды/объятия (Sequence)
        add(seed("secret_miss_you", "Я скучаю", "Двойное мигание сверху, затем низ", listOf("секрет", "объятия"), PresetPatterns.secretCodeMissYou(hardwareVersion)))
        add(seed("secret_thinking", "Думаю о тебе", "Тройная пульсация по кругу", listOf("секрет", "объятия"), PresetPatterns.secretCodeThinkingOfYou(hardwareVersion)))
        add(seed("secret_love_you", "Люблю тебя", "Сердцебиение", listOf("секрет", "объятия"), PresetPatterns.secretCodeLoveYou(hardwareVersion)))
        // Дополнительные последовательности
        add(seed(
            id = "secret_sos",
            title = "SOS",
            description = "Морзе ... --- ... красным",
            tags = listOf("секрет", "морзе", "тревога"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementSequence(
                        steps = buildList {
                            // S: ...
                            repeat(3) { add(SequenceStep.LedAction(ledIndex = 0, color = "#FF0000", durationMs = 150)); add(SequenceStep.DelayAction(100)) }
                            add(SequenceStep.DelayAction(250))
                            // O: ---
                            repeat(3) { add(SequenceStep.LedAction(4, "#FF0000", 450)); add(SequenceStep.DelayAction(150)) }
                            add(SequenceStep.DelayAction(250))
                            // S: ...
                            repeat(3) { add(SequenceStep.LedAction(0, "#FF0000", 150)); add(SequenceStep.DelayAction(100)) }
                        }
                    )
                )
            }
        ))

        // Smart Home (Timeline)
        add(seed(
            id = "smarthome_doorbell",
            title = "Дверной звонок",
            description = "Быстрое кольцевое мигание",
            tags = listOf("умныйдом", "уведомление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1200,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetRing,
                                mixMode = MixMode.OVERRIDE,
                                clips = listOf(
                                    TimelineClip(0, 300, "#FFD700", 50, 50, Easing.LINEAR),
                                    TimelineClip(400, 300, "#FFD700", 50, 50, Easing.LINEAR),
                                    TimelineClip(800, 300, "#FFD700", 50, 50, Easing.LINEAR)
                                )
                            )
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "smarthome_alarm",
            title = "Тревога",
            description = "Красно-белая сирена",
            tags = listOf("умныйдом", "будильник"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 2000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetRing,
                                mixMode = MixMode.OVERRIDE,
                                clips = listOf(
                                    TimelineClip(0, 500, "#FF0000", 0, 0),
                                    TimelineClip(500, 500, "#FFFFFF", 0, 0),
                                    TimelineClip(1000, 500, "#FF0000", 0, 0),
                                    TimelineClip(1500, 500, "#FFFFFF", 0, 0)
                                )
                            )
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "smarthome_timer_done",
            title = "Таймер завершён",
            description = "Зелёное заполнение и пульс",
            tags = listOf("умныйдом", "таймер"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1500,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(
                                target = TargetRing,
                                clips = listOf(
                                    TimelineClip(0, 800, "#00FF00", 100, 100),
                                    TimelineClip(800, 700, "#00FF00", 0, 100)
                                )
                            )
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "smarthome_washing_done",
            title = "Стирка завершена",
            description = "Сине-белые всполохи",
            tags = listOf("умныйдом", "приборы"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1800,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetGroup(listOf(0,2,4,6)), clips = listOf(
                                TimelineClip(0, 300, "#00AAFF", 50, 50),
                                TimelineClip(600, 300, "#FFFFFF", 50, 50),
                                TimelineClip(1200, 300, "#00AAFF", 50, 50)
                            )),
                            TimelineTrack(TargetGroup(listOf(1,3,5,7)), clips = listOf(
                                TimelineClip(300, 300, "#00AAFF", 50, 50),
                                TimelineClip(900, 300, "#FFFFFF", 50, 50),
                                TimelineClip(1500, 300, "#00AAFF", 50, 50)
                            ))
                        )
                    )
                )
            }
        ))

        // Практики (Timeline)
        add(seed(
            id = "practice_box_breath",
            title = "Box breathing 4-4-4-4",
            description = "Вдох/Задержка/Выдох/Задержка по 4с",
            tags = listOf("практика", "дыхание"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 16 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 16000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-4с
                                TimelineClip(0, 4000, "#00AAFF", 200, 200),
                                // Задержка 4-8с (мягкая подсветка)
                                TimelineClip(4000, 4000, "#003355", 0, 0),
                                // Выдох 8-12с
                                TimelineClip(8000, 4000, "#00AAFF", 200, 400),
                                // Задержка 12-16с
                                TimelineClip(12000, 4000, "#001122", 0, 0)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_breathing_478",
            title = "Дыхание 4-7-8",
            description = "Вдох 4с, задержка 7с, выдох 8с",
            tags = listOf("практика", "дыхание"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 19 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 19000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-4с
                                TimelineClip(0, 4000, "#4A90E2", 150, 150),
                                // Задержка 4-11с (очень мягкая подсветка)
                                TimelineClip(4000, 7000, "#1E3A5F", 0, 0),
                                // Выдох 11-19с (плавное угасание)
                                TimelineClip(11000, 8000, "#4A90E2", 150, 500)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_energy_breath",
            title = "Энергетическое дыхание Вима Хофа",
            description = "Быстрые вдохи-выдохи с задержками",
            tags = listOf("практика", "дыхание", "энергия"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 30 циклов по 2с + задержка 15с = 75с общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 75000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = buildList {
                                // 30 циклов быстрого дыхания (60 секунд)
                                repeat(30) { cycle ->
                                    val startMs = cycle * 2000
                                    add(TimelineClip(startMs, 1000, "#FF6B35", 100, 100)) // вдох
                                    add(TimelineClip(startMs + 1000, 1000, "#FF8C42", 100, 100)) // выдох
                                }
                                // Задержка на 15 секунд (красное свечение)
                                add(TimelineClip(60000, 15000, "#CC5533", 200, 200))
                            })
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_coherent_breath",
            title = "Когерентное дыхание 5-5",
            description = "Сбалансированное дыхание для HRV",
            tags = listOf("практика", "дыхание", "врв"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 10 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 10000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-5с
                                TimelineClip(0, 5000, "#00CED1", 180, 180),
                                // Выдох 5-10с
                                TimelineClip(5000, 5000, "#00CED1", 180, 180)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_belly_breath",
            title = "Диафрагмальное дыхание",
            description = "Глубокое брюхо-дыхание",
            tags = listOf("практика", "дыхание", "живот"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 12 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 12000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-6с (плавное заполнение)
                                TimelineClip(0, 6000, "#32CD32", 200, 200),
                                // Выдох 6-12с (плавное опустошение)
                                TimelineClip(6000, 6000, "#228B22", 200, 400)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_alternate_breath",
            title = "Попеременное дыхание",
            description = "Чередование ноздрей",
            tags = listOf("практика", "дыхание", "йога"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 16 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 16000,
                        tickMs = 100,
                        tracks = listOf(
                            // Левая ноздря (четные диоды)
                            TimelineTrack(TargetGroup(listOf(0, 2, 4, 6)), clips = listOf(
                                TimelineClip(0, 4000, "#9370DB", 150, 150), // вдох левая
                                TimelineClip(8000, 4000, "#9370DB", 150, 150), // выдох левая
                            )),
                            // Правая ноздря (нечетные диоды)
                            TimelineTrack(TargetGroup(listOf(1, 3, 5, 7)), clips = listOf(
                                TimelineClip(4000, 4000, "#8A2BE2", 150, 150), // вдох правая
                                TimelineClip(12000, 4000, "#8A2BE2", 150, 150) // выдох правая
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_ocean_breath",
            title = "Океаническое дыхание",
            description = "Уджайи с морскими волнами",
            tags = listOf("практика", "дыхание", "океан"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 8 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 8000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-4с (волна нарастает)
                                TimelineClip(0, 4000, "#4682B4", 200, 200),
                                // Выдох 4-8с (волна спадает)
                                TimelineClip(4000, 4000, "#5F9EA0", 200, 400)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_focus_square",
            title = "Фокус-квадрат 3-3-3-3",
            description = "Быстрое дыхание для концентрации",
            tags = listOf("практика", "дыхание", "фокус"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 12 секунд общий цикл
                addElement(
                    PatternElementTimeline(
                        durationMs = 12000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-3с
                                TimelineClip(0, 3000, "#FFD700", 250, 250),
                                // Задержка 3-6с
                                TimelineClip(3000, 3000, "#B8860B", 0, 0),
                                // Выдох 6-9с
                                TimelineClip(6000, 3000, "#FFD700", 250, 250),
                                // Задержка 9-12с
                                TimelineClip(9000, 3000, "#8B6914", 0, 0)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "practice_relax_ratio",
            title = "Расслабляющее дыхание 1:2",
            description = "Выдох в 2 раза длиннее вдоха",
            tags = listOf("практика", "дыхание", "расслабление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                // 9 секунд общий цикл (3с вдох, 6с выдох)
                addElement(
                    PatternElementTimeline(
                        durationMs = 9000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                // Вдох 0-3с
                                TimelineClip(0, 3000, "#87CEEB", 180, 180),
                                // Выдох 3-9с (плавное угасание)
                                TimelineClip(3000, 6000, "#87CEEB", 180, 500)
                            ))
                        )
                    )
                )
            }
        ))

        // Медитативные паттерны
        add(seed(
            id = "meditation_body_scan",
            title = "Сканирование тела",
            description = "Постепенное расслабление частей тела",
            tags = listOf("медитация", "тело", "расслабление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 20000,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = idx * 2000, durationMs = 3000, color = "#98FB98", fadeInMs = 500, fadeOutMs = 1000)
                                )
                            )
                        }
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_breath_awareness",
            title = "Осознанность дыхания",
            description = "Мягкая пульсация с дыханием",
            tags = listOf("медитация", "дыхание", "осознанность"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 8000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 4000, "#E6E6FA", 100, 100),
                                TimelineClip(4000, 4000, "#D8BFD8", 100, 100)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_metta",
            title = "Медитация любящей доброты",
            description = "Розовое золотое свечение",
            tags = listOf("медитация", "любовь", "сострадание"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 10000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 5000, "#FFB6C1", 200, 200),
                                TimelineClip(5000, 5000, "#FFC0CB", 200, 200)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_zazen",
            title = "Дзадзэн",
            description = "Стабильное белое свечение",
            tags = listOf("медитация", "дзен", "стабильность"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 15000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 15000, "#F5F5F5", 50, 50)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_walking",
            title = "Осознанная ходьба",
            description = "Шагающие огоньки",
            tags = listOf("медитация", "ходьба", "движение"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 4000,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = idx * 500, durationMs = 1000, color = "#90EE90", fadeInMs = 200, fadeOutMs = 300)
                                )
                            )
                        }
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_chakra",
            title = "Балансировка чакр",
            description = "Цвета радуги по чакрам",
            tags = listOf("медитация", "чакры", "энергия"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 14000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 2000, "#FF0000", 150, 150), // красный
                                TimelineClip(2000, 2000, "#FFA500", 150, 150), // оранжевый
                                TimelineClip(4000, 2000, "#FFFF00", 150, 150), // желтый
                                TimelineClip(6000, 2000, "#00FF00", 150, 150), // зеленый
                                TimelineClip(8000, 2000, "#0000FF", 150, 150), // синий
                                TimelineClip(10000, 2000, "#4B0082", 150, 150), // индиго
                                TimelineClip(12000, 2000, "#9400D3", 150, 150) // фиолетовый
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_muscle_relax",
            title = "Прогрессивная релаксация",
            description = "Напряжение и расслабление",
            tags = listOf("медитация", "мышцы", "расслабление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 8000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 2000, "#FF6347", 200, 0), // напряжение
                                TimelineClip(2000, 4000, "#87CEEB", 0, 200), // расслабление
                                TimelineClip(6000, 2000, "#FF6347", 200, 0) // напряжение
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "meditation_yoga_nidra",
            title = "Йога-нидра",
            description = "Глубокое трансоceanское свечение",
            tags = listOf("медитация", "сон", "глубокий"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 20000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 20000, "#191970", 100, 100)
                            ))
                        )
                    )
                )
            }
        ))

        // Звуковые паттерны
        add(seed(
            id = "sound_rain",
            title = "Звук дождя",
            description = "Синие капли",
            tags = listOf("звук", "дождь", "природа"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 3000,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = (idx * 375) % 3000, durationMs = 300, color = "#4682B4", fadeInMs = 50, fadeOutMs = 150)
                                )
                            )
                        }
                    )
                )
            }
        ))
        add(seed(
            id = "sound_forest",
            title = "Лес с птицами",
            description = "Зеленые и желтые вспышки",
            tags = listOf("звук", "лес", "птицы"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 5000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 5000, "#228B22", 80, 80),
                                TimelineClip(1000, 200, "#FFD700", 100, 100),
                                TimelineClip(2500, 300, "#FFD700", 100, 100),
                                TimelineClip(4000, 250, "#FFD700", 100, 100)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_ocean",
            title = "Океанские волны",
            description = "Синие волны",
            tags = listOf("звук", "океан", "волны"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 6000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 3000, "#006994", 150, 150),
                                TimelineClip(3000, 3000, "#00BFFF", 150, 150)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_fireplace",
            title = "Камин",
            description = "Оранжево-красные огоньки",
            tags = listOf("звук", "огонь", "уют"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 2000,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = (idx * 250) % 2000, durationMs = 400, color = "#FF8C00", fadeInMs = 100, fadeOutMs = 200)
                                )
                            )
                        }
                    )
                )
            }
        ))
        add(seed(
            id = "sound_space",
            title = "Космический эмбиент",
            description = "Фиолетовые пульсации",
            tags = listOf("звук", "космос", "эмбиент"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 8000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 8000, "#4B0082", 120, 120)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_tibetan",
            title = "Тибетские чаши",
            description = "Золотые резонансы",
            tags = listOf("звук", "чаши", "исцеление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 4000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 2000, "#FFD700", 200, 200),
                                TimelineClip(2000, 2000, "#FFA500", 200, 200)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_binaural",
            title = "Бинауральные ритмы",
            description = "Тета-волны",
            tags = listOf("звук", "бинауральные", "тета"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 6000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 6000, "#8A2BE2", 100, 100)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_white_noise",
            title = "Белый шум",
            description = "Стабильный серый свет",
            tags = listOf("звук", "белыйшум", "фокус"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 10000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 10000, "#C0C0C0", 60, 60)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_thunder",
            title = "Гроза",
            description = "Молнии и гром",
            tags = listOf("звук", "гром", "буря"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 8000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 200, "#FFFFFF", 300, 100), // молния
                                TimelineClip(200, 1800, "#4B0082", 100, 100), // после молнии
                                TimelineClip(3000, 150, "#FFFFFF", 300, 100), // вторая молния
                                TimelineClip(3150, 2850, "#4B0082", 100, 100),
                                TimelineClip(6000, 100, "#FFFFFF", 300, 100), // третья молния
                                TimelineClip(6100, 1900, "#4B0082", 100, 100)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "sound_wind_chimes",
            title = "Ветровые колокольчики",
            description = "Серебристые звуки",
            tags = listOf("звук", "ветер", "колокольчики"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 4000,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = (idx * 500) % 4000, durationMs = 600, color = "#E0E0E0", fadeInMs = 150, fadeOutMs = 300)
                                )
                            )
                        }
                    )
                )
            }
        ))

        // Объятия (Timeline + Sequence)
        add(seed(
            id = "hugs_wave",
            title = "Объятие-волна",
            description = "Волна по кругу мягким розовым",
            tags = listOf("объятия"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1600,
                        tickMs = 100,
                        tracks = (0..7).map { idx ->
                            TimelineTrack(
                                target = TargetLed(idx),
                                clips = listOf(
                                    TimelineClip(startMs = idx * 150, durationMs = 300, color = "#FF69B4", fadeInMs = 50, fadeOutMs = 100)
                                )
                            )
                        }
                    )
                )
            }
        ))
        add(seed(
            id = "hugs_heartbeat",
            title = "Объятие-сердцебиение",
            description = "Двойной удар сердцем",
            tags = listOf("объятия"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1200,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 200, "#FF3366"),
                                TimelineClip(250, 200, "#FF3366"),
                                TimelineClip(700, 150, "#FF99BB")
                            ))
                        )
                    )
                )
            }
        ))

        // Курсы (сигналы элементов курса)
        add(seed(
            id = "course_focus_intro",
            title = "Курс Фокус: вступление",
            description = "Короткая зелёная индикация",
            tags = listOf("курс", "фокус"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1500,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 800, "#00FF00", 100, 100),
                                TimelineClip(900, 600, "#00DD00", 50, 150)
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "course_relax_finish",
            title = "Курс Расслабление: завершение",
            description = "Тёплый золотой фейд-аут",
            tags = listOf("курс", "расслабление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 2000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 2000, "#FFD27F", 200, 500)
                            ))
                        )
                    )
                )
            }
        ))

        // Нотификации
        add(seed(
            id = "notify_incoming_call",
            title = "Входящий звонок",
            description = "Чередование зелёный/белый",
            tags = listOf("уведомление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1500,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 500, "#00FF00"),
                                TimelineClip(500, 500, "#FFFFFF"),
                                TimelineClip(1000, 500, "#00FF00")
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "notify_email",
            title = "Email",
            description = "Синий двойной блик",
            tags = listOf("уведомление"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                addElement(
                    PatternElementTimeline(
                        durationMs = 900,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 300, "#1E90FF", 50, 50),
                                TimelineClip(450, 300, "#1E90FF", 50, 50)
                            ))
                        )
                    )
                )
            }
        ))

        // Прочие утилиты
        add(seed(
            id = "util_night_light",
            title = "Ночной свет",
            description = "Тёплый тусклый свет",
            tags = listOf("утилиты"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 5000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetRing, clips = listOf(
                                TimelineClip(0, 5000, "#332211")
                            ))
                        )
                    )
                )
            }
        ))
        add(seed(
            id = "util_timer_tick",
            title = "Тикер таймера",
            description = "Короткий блик каждую секунду",
            tags = listOf("утилиты", "таймер"),
            specJsonFrom = buildPattern {
                setHardwareVersion(hardwareVersion)
                setLoop(true)
                addElement(
                    PatternElementTimeline(
                        durationMs = 1000,
                        tickMs = 100,
                        tracks = listOf(
                            TimelineTrack(TargetLed(0), clips = listOf(
                                TimelineClip(0, 120, "#AAAAAA", 20, 40)
                            ))
                        )
                    )
                )
            }
        ))
    }

    private fun seed(
        id: String,
        title: String,
        description: String?,
        tags: List<String>,
        specJsonFrom: com.example.amulet.shared.domain.patterns.model.PatternSpec,
        public: Boolean = false
    ): PatternSeed = PatternSeed(
        id = id,
        title = title,
        description = description,
        kind = "LIGHT",
        spec = specJsonFrom,
        public = public,
        tags = tags,
        ownerId = null, // Пресеты не принадлежат пользователю
        sharedWith = emptyList()
    )
}
