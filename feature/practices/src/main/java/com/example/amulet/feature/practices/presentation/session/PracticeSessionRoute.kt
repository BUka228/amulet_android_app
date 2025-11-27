package com.example.amulet.feature.practices.presentation.session

import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.shared.domain.practices.model.PracticeSessionStatus

@Composable
fun PracticeSessionRoute(
    practiceId: String,
    onNavigateBack: () -> Unit,
    viewModel: PracticeSessionViewModel = hiltViewModel(),
    onOpenSchedule: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        val isActive = state.session?.status == PracticeSessionStatus.ACTIVE

        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(state.title ?: "Практика") },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.handleIntent(PracticeSessionIntent.Stop(completed = false))
                                onNavigateBack()
                            }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 3.dp,
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Button(
                                onClick = {
                                    if (isActive) {
                                        viewModel.handleIntent(PracticeSessionIntent.Stop(completed = true))
                                    } else {
                                        viewModel.handleIntent(PracticeSessionIntent.Start)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(text = if (isActive) "Завершить" else "Начать")
                            }
                        }
                    }
                },
            )
        }
    }

    LaunchedEffect(practiceId) {
        viewModel.setPracticeIdIfEmpty(practiceId)
    }

    PracticeSessionScreen(
        state = state,
        onIntent = viewModel::handleIntent,
        onNavigateBack = onNavigateBack,
    )
}
