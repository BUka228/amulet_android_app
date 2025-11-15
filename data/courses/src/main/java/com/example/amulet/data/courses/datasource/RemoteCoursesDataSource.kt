package com.example.amulet.data.courses.datasource

import com.example.amulet.core.database.entity.CourseEntity
import com.example.amulet.core.database.entity.CourseItemEntity
import com.example.amulet.shared.core.AppResult
import com.github.michaelbull.result.Ok
import javax.inject.Inject

interface RemoteCoursesDataSource {
    suspend fun refreshCatalog(): AppResult<Unit>
}

class RemoteCoursesDataSourceStub @Inject constructor() : RemoteCoursesDataSource {
    override suspend fun refreshCatalog(): AppResult<Unit> = Ok(Unit)
}
