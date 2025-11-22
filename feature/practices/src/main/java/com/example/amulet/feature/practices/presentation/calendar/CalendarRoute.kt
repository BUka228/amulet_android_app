package com.example.amulet.feature.practices.presentation.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CalendarRoute(
    initialPracticeId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToPractice: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialPracticeId) {
        if (initialPracticeId != null) {
            viewModel.onIntent(CalendarIntent.OpenPlanner(initialPracticeId))
        }
        viewModel.effects.collect { effect ->
            when (effect) {
                CalendarEffect.NavigateBack -> onNavigateBack()
                is CalendarEffect.NavigateToPractice -> onNavigateToPractice(effect.practiceId)
            }
        }
    }

    CalendarScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}
