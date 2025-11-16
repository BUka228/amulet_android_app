package com.example.amulet_android_app.initialization

import android.content.Context
import android.content.SharedPreferences
import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.core.logging.Logger
import com.example.amulet.shared.domain.initialization.DataInitializer
import com.example.amulet.shared.domain.initialization.usecase.SeedLocalDataUseCase
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация инициализатора данных в app модуле.
 * Использует UseCase для локального сидирования данных после авторизации пользователя.
 */
@Singleton
class DataInitializerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val seedLocalDataUseCase: SeedLocalDataUseCase
) : DataInitializer {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun initializeIfNeeded(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isDataInitialized()) {
                Logger.d("Данные уже инициализированы ранее", "DataInitializerImpl")
                return@withContext Ok(Unit)
            }

            Logger.d("Начинаем локальное сидирование данных при инициализации приложения", "DataInitializerImpl")
            
            // Выполняем локальное сидирование через UseCase
            val result = seedLocalDataUseCase()
            
            result.onSuccess {
                markDataAsInitialized()
                Logger.d("Локальное сидирование данных завершено успешно", "DataInitializerImpl")
            }.onFailure { error ->
                Logger.e("Ошибка при локальном сидировании данных: $error", tag = "DataInitializerImpl")
            }
            
            result
            
        } catch (e: Exception) {
            Logger.e("Критическая ошибка при инициализации данных: $e", throwable = e, tag = "DataInitializerImpl")
            Err(com.example.amulet.shared.core.AppError.Unknown)
        }
    }

    private fun isDataInitialized(): Boolean {
        return sharedPreferences.getBoolean(KEY_DATA_INITIALIZED, false)
    }

    private fun markDataAsInitialized() {
        sharedPreferences.edit()
            .putBoolean(KEY_DATA_INITIALIZED, true)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "app_initialization_prefs"
        private const val KEY_DATA_INITIALIZED = "data_initialized"
    }
}
