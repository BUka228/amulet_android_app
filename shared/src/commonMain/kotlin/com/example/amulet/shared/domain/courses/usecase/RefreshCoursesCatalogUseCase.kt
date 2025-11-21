package com.example.amulet.shared.domain.courses.usecase

import com.example.amulet.shared.core.AppResult
import com.example.amulet.shared.domain.courses.CoursesRepository
class RefreshCoursesCatalogUseCase(
    private val repository: CoursesRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        return repository.refreshCatalog()
    }
}
