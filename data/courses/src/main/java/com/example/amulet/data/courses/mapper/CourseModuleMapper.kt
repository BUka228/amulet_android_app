package com.example.amulet.data.courses.mapper

import com.example.amulet.core.database.entity.CourseModuleEntity
import com.example.amulet.shared.domain.courses.model.CourseModule
import com.example.amulet.shared.domain.courses.model.CourseModuleId

fun CourseModuleEntity.toDomain(): CourseModule = CourseModule(
    id = CourseModuleId(id),
    courseId = courseId,
    order = order,
    title = title,
    description = description,
    recommendedDayOffset = recommendedDayOffset
)
