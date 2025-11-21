package com.example.amulet.feature.practices.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.practices.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeDetailsRoute(
    practiceId: String,
    onNavigateBack: () -> Unit,
    onNavigateToPattern: (String) -> Unit,
    onNavigateToPlan: (String) -> Unit,
    viewModel: PracticeDetailsViewModel = hiltViewModel()
) {
    viewModel.setIdIfEmpty(practiceId)
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PracticeDetailsEffect.NavigateToPattern -> onNavigateToPattern(effect.patternId)
                is PracticeDetailsEffect.NavigateBack -> onNavigateBack()
                is PracticeDetailsEffect.NavigateToPlan -> onNavigateToPlan(effect.practiceId)
            }
        }
    }

    val scaffoldState = LocalScaffoldState.current
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(state.practice?.title.orEmpty()) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.handleIntent(PracticeDetailsIntent.NavigateBack) }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        actions = {
                            val isFavorite = state.isFavorite
                            IconButton(onClick = { viewModel.handleIntent(PracticeDetailsIntent.ToggleFavorite) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    PracticeDetailsBottomBar(
                        isEnabled = state.practice != null && !state.isLoading,
                        isFavorite = state.isFavorite,
                        onStart = { viewModel.handleIntent(PracticeDetailsIntent.StartPractice) },
                        onAddToPlan = { viewModel.handleIntent(PracticeDetailsIntent.AddToPlan) },
                        onToggleFavorite = { viewModel.handleIntent(PracticeDetailsIntent.ToggleFavorite) }
                    )
                }
            )
        }
    }

    PracticeDetailsScreen(
        state = state,
        onIntent = viewModel::handleIntent
    )
}

@Composable
private fun PracticeDetailsScreen(
    state: PracticeDetailsState,
    onIntent: (PracticeDetailsIntent) -> Unit
) {
    val practice = state.practice
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (practice != null) {
            HeroSection(practice = practice)

            practice.description?.takeIf { it.isNotBlank() }?.let {
                AboutSection(description = it)
            }

            if (practice.audioUrl != null) {
                AudioInfoSection()
            }

            AmuletSection(
                pattern = state.pattern,
                onOpenPattern = { onIntent(PracticeDetailsIntent.OpenPattern) }
            )

            if (practice.contraindications.isNotEmpty()) {
                SafetySection(contraindications = practice.contraindications)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

}

@Composable
private fun HeroSection(practice: com.example.amulet.shared.domain.practices.model.Practice) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val coverColor = when (practice.goal) {
            com.example.amulet.shared.domain.practices.model.PracticeGoal.SLEEP -> MaterialTheme.colorScheme.primaryContainer
            com.example.amulet.shared.domain.practices.model.PracticeGoal.STRESS -> MaterialTheme.colorScheme.tertiaryContainer
            com.example.amulet.shared.domain.practices.model.PracticeGoal.ENERGY -> MaterialTheme.colorScheme.secondaryContainer
            com.example.amulet.shared.domain.practices.model.PracticeGoal.FOCUS -> MaterialTheme.colorScheme.primary
            com.example.amulet.shared.domain.practices.model.PracticeGoal.RELAXATION -> MaterialTheme.colorScheme.secondary
            com.example.amulet.shared.domain.practices.model.PracticeGoal.ANXIETY -> MaterialTheme.colorScheme.errorContainer
            com.example.amulet.shared.domain.practices.model.PracticeGoal.MOOD -> MaterialTheme.colorScheme.secondaryContainer
            null -> MaterialTheme.colorScheme.surfaceVariant
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.large)
                .background(coverColor)
        )

        Text(
            text = practice.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            practice.goal?.let { goal ->
                val resId = goalTitleRes(goal)
                AssistChip(
                    onClick = {},
                    label = { Text(text = stringResource(id = resId)) }
                )
            }

            val typeText = when (practice.type) {
                com.example.amulet.shared.domain.practices.model.PracticeType.BREATH -> stringResource(id = R.string.practice_type_breath)
                com.example.amulet.shared.domain.practices.model.PracticeType.MEDITATION -> stringResource(id = R.string.practice_type_meditation)
                com.example.amulet.shared.domain.practices.model.PracticeType.SOUND -> stringResource(id = R.string.practice_type_sound)
                else -> null
            }
            typeText?.let {
                AssistChip(
                    onClick = {},
                    label = { Text(text = it) }
                )
            }

            practice.durationSec?.let { durationSec ->
                val minutes = durationSec / 60
                val durationText = stringResource(id = R.string.practices_home_duration_minutes, minutes)
                AssistChip(
                    onClick = {},
                    label = { Text(text = durationText) }
                )
            }

            practice.level?.let { level ->
                val levelText = when (level) {
                    com.example.amulet.shared.domain.practices.model.PracticeLevel.BEGINNER -> stringResource(id = R.string.practice_level_beginner)
                    com.example.amulet.shared.domain.practices.model.PracticeLevel.INTERMEDIATE -> stringResource(id = R.string.practice_level_intermediate)
                    com.example.amulet.shared.domain.practices.model.PracticeLevel.ADVANCED -> stringResource(id = R.string.practice_level_advanced)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(text = levelText) }
                )
            }

            if (practice.usageCount > 100) {
                AssistChip(
                    onClick = {},
                    label = { Text(text = stringResource(id = R.string.practice_badge_popular)) }
                )
            }
        }
    }
}

@Composable
private fun AboutSection(description: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.practice_details_about),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AudioInfoSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Практика содержит аудио‑сопровождение",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AmuletSection(
    pattern: com.example.amulet.shared.domain.patterns.model.Pattern?,
    onOpenPattern: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.practice_details_amulet),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onOpenPattern, enabled = pattern != null) {
                Text(text = stringResource(id = R.string.practice_details_open_pattern))
            }
        }

        if (pattern != null) {
            Text(
                text = pattern.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = stringResource(id = R.string.practice_details_pattern_auto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SafetySection(contraindications: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = stringResource(id = R.string.practice_details_safety),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        contraindications.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PracticeDetailsBottomBar(
    isEnabled: Boolean,
    onStart: () -> Unit,
    onAddToPlan: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Button(
                onClick = onAddToPlan,
                enabled = isEnabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.practice_details_add_to_plan))
            }

            Button(
                onClick = onStart,
                enabled = isEnabled,
                modifier = Modifier.weight(1.4f)
            ) {
                Text(text = stringResource(id = R.string.practice_details_start))
            }
        }
    }
}

@Composable
private fun goalTitleRes(goal: com.example.amulet.shared.domain.practices.model.PracticeGoal): Int = when (goal) {
    com.example.amulet.shared.domain.practices.model.PracticeGoal.SLEEP -> R.string.practice_goal_sleep
    com.example.amulet.shared.domain.practices.model.PracticeGoal.STRESS -> R.string.practice_goal_stress
    com.example.amulet.shared.domain.practices.model.PracticeGoal.ENERGY -> R.string.practice_goal_energy
    com.example.amulet.shared.domain.practices.model.PracticeGoal.FOCUS -> R.string.practice_goal_focus
    com.example.amulet.shared.domain.practices.model.PracticeGoal.RELAXATION -> R.string.practice_goal_relaxation
    com.example.amulet.shared.domain.practices.model.PracticeGoal.ANXIETY -> R.string.practice_goal_anxiety
    com.example.amulet.shared.domain.practices.model.PracticeGoal.MOOD -> R.string.practice_goal_mood
}

