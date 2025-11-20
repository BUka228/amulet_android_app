package com.example.amulet.feature.practices.presentation.search

import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.practices.model.Practice

data class PracticesSearchState(
    val query: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val results: List<Practice> = emptyList(),
)
