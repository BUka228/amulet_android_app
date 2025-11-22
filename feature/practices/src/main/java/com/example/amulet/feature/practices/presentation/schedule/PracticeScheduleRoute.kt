package com.example.amulet.feature.practices.presentation.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun PracticeScheduleRoute(
    practiceId: String,
    onNavigateBack: () -> Unit,
    viewModel: PracticeScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PracticeScheduleEffect.Back -> onNavigateBack()
            }
        }
    }

    PracticeScheduleScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeScheduleScreen(
    state: PracticeScheduleState,
    onIntent: (PracticeScheduleIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.practiceTitle.ifBlank { "План практики" },
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onIntent(PracticeScheduleIntent.NavigateBack) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Выберите дни недели",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val days = listOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")
                days.forEach { (day, title) ->
                    val selected = day in state.selectedDays
                    FilterChip(
                        selected = selected,
                        onClick = { onIntent(PracticeScheduleIntent.ToggleDay(day)) },
                        label = { Text(title) }
                    )
                }
            }

            Text(
                text = "Время",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null)
                TextField(
                    value = state.timeOfDay,
                    onValueChange = { onIntent(PracticeScheduleIntent.ChangeTime(it)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Напоминание", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Присылать уведомление перед практикой",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = state.reminderEnabled,
                    onCheckedChange = { onIntent(PracticeScheduleIntent.SetReminderEnabled(it)) }
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error.toString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onIntent(PracticeScheduleIntent.NavigateBack) },
                    enabled = !state.isSaving
                ) {
                    Text(text = "Отменить")
                }

                Button(
                    onClick = { onIntent(PracticeScheduleIntent.Save) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving && state.selectedDays.isNotEmpty()
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Сохранить в план")
                    }
                }
            }
        }
    }
}
