package com.example.amulet.shared.domain.hugs

import com.example.amulet.shared.domain.patterns.PatternsRepository
import com.example.amulet.shared.domain.patterns.model.Pattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case для получения «секретных кодов» пары.
 *
 * На уровне домена секретные коды — это обычные паттерны,
 * помеченные специальным тегом "secret_code".
 * Привязка к действиям/сценариям может быть реализована через дополнительные теги.
 */
class GetSecretCodesUseCase(
    private val patternsRepository: PatternsRepository,
) {

    operator fun invoke(): Flow<List<Pattern>> {
        // Берём свои паттерны и фильтруем по тегу secret_code.
        return patternsRepository.getMyPatternsStream()
            .map { patterns ->
            patterns.filter { pattern ->
                pattern.tags.any { it.equals("secret_code", ignoreCase = true) }
            }
        }
    }
}
