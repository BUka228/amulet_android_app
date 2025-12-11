package com.example.amulet.shared.domain.patterns.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.patterns.model.PatternId
import com.example.amulet.shared.domain.patterns.model.PatternMarkers
import com.example.amulet.shared.domain.patterns.model.PatternWithSegments
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/**
 * Фасад для экрана редактора паттернов.
 * Инкапсулирует загрузку базового паттерна, его сегментов и работу с маркерами.
 */
class PatternEditorFacade(
    private val getPatternById: GetPatternByIdUseCase,
    private val getPatternSegments: GetPatternSegmentsUseCase,
    private val upsertPatternMarkers: UpsertPatternMarkersUseCase,
    private val applyPatternSegmentation: ApplyPatternSegmentationUseCase,
) {

    /**
     * Наблюдать за паттерном и его сегментами.
     * При ошибке загрузки сегментов вернём пустой список, ошибку залогируем.
     */
    fun getPatternWithSegments(patternId: PatternId): Flow<PatternWithSegments> {
        return getPatternById(patternId)
            .filterNotNull()
            .flatMapLatest { base ->
                flow {
                    var segments = emptyList<com.example.amulet.shared.domain.patterns.model.Pattern>()
                    getPatternSegments(base.id)
                        .onSuccess { segments = it }
                        .onFailure { error ->
                            Logger.e(
                                message = "Ошибка загрузки сегментов паттерна в фасаде редактора: $patternId",
                                throwable = Exception(error.toString()),
                                tag = "PatternEditorFacade"
                            )
                        }
                    emit(PatternWithSegments(base = base, segments = segments))
                }
            }
    }

    /**
     * Обновить (пересохранить) маркеры для паттерна.
     */
    suspend fun updateMarkers(patternId: PatternId, markersMs: List<Int>): AppResult<Unit> {
        val normalized = markersMs.filter { it >= 0 }.sorted().distinct()
        return upsertPatternMarkers(
            PatternMarkers(
                patternId = patternId,
                markersMs = normalized
            )
        )
    }

    /**
     * Нарезать паттерн на сегменты по маркерам.
     * Сначала сохраняем маркеры, затем применяем сегментацию.
     */
    suspend fun slicePattern(patternId: PatternId, markersMs: List<Int>): AppResult<Unit> {
        val normalized = markersMs.filter { it >= 0 }.sorted().distinct()
        return updateMarkers(patternId, normalized).andThen {
            applyPatternSegmentation(patternId, normalized)
        }
    }
}
