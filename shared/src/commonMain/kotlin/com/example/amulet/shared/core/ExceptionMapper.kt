package com.example.amulet.shared.core

fun interface ExceptionMapper {
    fun mapToAppError(throwable: Throwable): AppError
}

fun Throwable.toAppError(mapper: ExceptionMapper): AppError = mapper.mapToAppError(this)
