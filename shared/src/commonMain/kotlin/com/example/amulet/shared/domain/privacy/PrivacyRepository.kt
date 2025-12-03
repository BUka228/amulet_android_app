package com.example.amulet.shared.domain.privacy

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlinx.coroutines.flow.Flow

interface PrivacyRepository {

    /**
     * Возвращает текущие согласия пользователя.
     *
     * Конкретный источник (локальный кэш, сеть) определяется реализацией.
     */
    fun getUserConsentsStream(): Flow<UserConsents>

    /**
     * Обновляет согласия пользователя на backend и в локальном кэше.
     */
    suspend fun updateUserConsents(consents: UserConsents): AppResult<Unit>

    /**
     * Запускает процесс экспорта данных пользователя (right to access).
     */
    suspend fun requestDataExport(): AppResult<Unit>

    /**
     * Запускает процесс удаления аккаунта и связанных данных (right to erasure).
     */
    suspend fun requestAccountDeletion(): AppResult<Unit>
}
