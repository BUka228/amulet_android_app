package com.example.amulet.feature.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.button.ButtonSize
import com.example.amulet.core.design.components.button.ButtonVariant
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.components.card.CardElevation
import com.example.amulet.core.design.foundation.color.AmuletPalette
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import com.example.amulet.feature.dashboard.presentation.DailyStats
import com.example.amulet.feature.dashboard.presentation.DashboardSideEffect
import com.example.amulet.feature.dashboard.presentation.DashboardUiEvent
import com.example.amulet.feature.dashboard.presentation.DashboardUiState
import com.example.amulet.feature.dashboard.presentation.DashboardViewModel
import kotlin.math.sin

@Composable
fun DashboardRoute(
    onNavigateToDeviceDetails: (String) -> Unit,
    onNavigateToDevicesList: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToHugs: () -> Unit,
    onNavigateToPatterns: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is DashboardSideEffect.NavigateToDeviceDetails -> {
                    onNavigateToDeviceDetails(effect.deviceId)
                }
                DashboardSideEffect.NavigateToDevicesList -> {
                    onNavigateToDevicesList()
                }
                DashboardSideEffect.NavigateToPairing -> onNavigateToPairing()
                DashboardSideEffect.NavigateToLibrary -> onNavigateToLibrary()
                DashboardSideEffect.NavigateToHugs -> onNavigateToHugs()
                DashboardSideEffect.NavigateToPatterns -> onNavigateToPatterns()
                DashboardSideEffect.NavigateToSettings -> onNavigateToSettings()
                is DashboardSideEffect.ShowToast -> {
                    // TODO: Show toast when Snackbar/Toast system is implemented
                    println(effect.message)
                }
                is DashboardSideEffect.StartPracticeSession -> {
                    // TODO: Navigate to practice session screen
                    println("Navigate to practice session: ${effect.practiceId}")
                }
            }
        }
    }

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::handleEvent
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    val spacing = AmuletTheme.spacing
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = spacing.md)
            .padding(top = spacing.lg, bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Компактный header с приветствием
        DashboardHeader(
            userName = uiState.userName
        )

        // Устройства - компактная версия
        DevicesSection(
            devices = uiState.devices,
            connectedDevice = uiState.connectedDevice,
            onDeviceClick = { deviceId -> onEvent(DashboardUiEvent.DeviceClicked(deviceId)) },
            onNavigateToPairing = { onEvent(DashboardUiEvent.NavigateToPairing) },
            onNavigateToDevicesList = { onEvent(DashboardUiEvent.NavigateToDevicesList) }
        )

        // Статистика дня - улучшенная версия
        DailyStatsSection(
            stats = uiState.dailyStats
        )

        // Быстрый старт практики - обновленный дизайн
        QuickStartSection(
            onStartPractice = { practiceId -> onEvent(DashboardUiEvent.StartPractice(practiceId)) }
        )

        // Рекомендации на основе данных
        RecommendationsSection(
            stats = uiState.dailyStats,
            onStartPractice = { practiceId -> onEvent(DashboardUiEvent.StartPractice(practiceId)) }
        )

        Spacer(modifier = Modifier.height(spacing.sm))
    }
}

@Composable
private fun DashboardHeader(
    userName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_welcome),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RecommendationsSection(
    stats: DailyStats,
    onStartPractice: (String) -> Unit
) {
    val spacing = AmuletTheme.spacing
    
    // Определяем рекомендацию на основе статистики
    val recommendation = when {
        stats.practiceMinutes < 5 -> RecommendationData(
            titleRes = R.string.recommendation_start_day,
            descriptionRes = R.string.recommendation_start_day_description,
            practiceId = "breathing_4_7_8",
            icon = Icons.Default.FavoriteBorder
        )
        stats.calmLevel < 60 -> RecommendationData(
            titleRes = R.string.recommendation_restore_calm,
            descriptionRes = R.string.recommendation_restore_calm_description,
            practiceId = "meditation_calm",
            icon = Icons.Default.Favorite
        )
        else -> null
    }
    
    recommendation?.let {
        AmuletCard(
            elevation = CardElevation.Default,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = null,
                        tint = AmuletPalette.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.md))
                    Column {
                        Text(
                            text = stringResource(it.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(it.descriptionRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(spacing.sm))
                
                AmuletButton(
                    text = stringResource(R.string.recommendation_button_start),
                    onClick = { onStartPractice(it.practiceId) },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small
                )
            }
        }
    }
}

private data class RecommendationData(
    val titleRes: Int,
    val descriptionRes: Int,
    val practiceId: String,
    val icon: ImageVector
)
