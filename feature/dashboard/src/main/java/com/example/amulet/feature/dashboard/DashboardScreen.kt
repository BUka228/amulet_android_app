package com.example.amulet.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun DashboardRoute(
    onNavigateToDeviceDetails: (String) -> Unit,
    onNavigateToDevicesList: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToHugs: () -> Unit,
    onNavigateToPatterns: () -> Unit,
    onNavigateToPracticeSession: (String) -> Unit,
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
                    onNavigateToPracticeSession(effect.practiceId)
                }
            }
        }
    }

    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::handleEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    val spacing = AmuletTheme.spacing
    val scaffoldState = LocalScaffoldState.current

    // Устанавливаем TopBar синхронно
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    DashboardTopBar(
                        userName = uiState.userName,
                        onSettingsClick = { onEvent(DashboardUiEvent.NavigateToSettings) },
                    )
                },
                floatingActionButton = {} // Обнуляем FAB
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        item {
            Spacer(modifier = Modifier.height(spacing.xs))
        }

        item {
            // Компактная статистика в верхней части
            CompactStatsRow(
                stats = uiState.dailyStats
            )
        }

        item {
            // Устройства - компактная версия
            DevicesSection(
                devices = uiState.devices,
                connectedDevice = uiState.connectedDevice,
                connectedBatteryLevel = uiState.connectedBatteryLevel,
                onDeviceClick = { deviceId -> onEvent(DashboardUiEvent.DeviceClicked(deviceId)) },
                onNavigateToPairing = { onEvent(DashboardUiEvent.NavigateToPairing) },
                onNavigateToDevicesList = { onEvent(DashboardUiEvent.NavigateToDevicesList) }
            )
        }

        if (uiState.quickStartPracticeId != null) {
            item {
                // Быстрый старт практики - обновленный дизайн
                QuickStartSection(
                    onStartPractice = { onEvent(DashboardUiEvent.StartPractice(uiState.quickStartPracticeId)) }
                )
            }
        }

        item {
            // Рекомендации на основе данных
            RecommendationsSection(
                stats = uiState.dailyStats,
                recommendedPracticeId = uiState.recommendedPracticeId,
                onStartPractice = { practiceId -> onEvent(DashboardUiEvent.StartPractice(practiceId)) }
            )
        }

        item {
            // Быстрый доступ к разделам
            QuickAccessGrid(
                onNavigateToLibrary = { onEvent(DashboardUiEvent.NavigateToLibrary) },
                onNavigateToHugs = { onEvent(DashboardUiEvent.NavigateToHugs) },
                onNavigateToPatterns = { onEvent(DashboardUiEvent.NavigateToPatterns) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(spacing.sm))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    userName: String?,
    onSettingsClick: () -> Unit
) {
    
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_hello),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = userName ?: stringResource(R.string.dashboard_guest_user),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.dashboard_settings)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun CompactStatsRow(
    stats: DailyStats
) {
    val spacing = AmuletTheme.spacing
    
    AmuletCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardElevation.Low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CompactStatItem(
                icon = Icons.Default.Timer,
                value = "${stats.practiceMinutes}",
                label = stringResource(R.string.dashboard_practices_short),
                color = AmuletPalette.Primary
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            CompactStatItem(
                icon = Icons.Default.Favorite,
                value = "${stats.hugsCount}",
                label = stringResource(R.string.dashboard_hugs_short),
                color = AmuletPalette.EmotionLove
            )
            
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            CompactStatItem(
                icon = Icons.Default.FavoriteBorder,
                value = "${stats.calmLevel}%",
                label = stringResource(R.string.dashboard_calm_short),
                color = AmuletPalette.EmotionCalm
            )
        }
    }
}

@Composable
private fun CompactStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecommendationsSection(
    stats: DailyStats,
    recommendedPracticeId: String?,
    onStartPractice: (String) -> Unit
) {
    val spacing = AmuletTheme.spacing
    
    // Определяем рекомендацию на основе статистики
    val recommendation = when {
        recommendedPracticeId != null && stats.calmLevel < 60 -> RecommendationData(
            titleRes = R.string.recommendation_restore_calm,
            descriptionRes = R.string.recommendation_restore_calm_description,
            icon = Icons.Default.Favorite
        )
        recommendedPracticeId != null -> RecommendationData(
            titleRes = R.string.recommendation_start_day,
            descriptionRes = R.string.recommendation_start_day_description,
            icon = Icons.Default.FavoriteBorder
        )
        else -> null
    }
    
    recommendation?.let {
        AmuletCard(
            elevation = CardElevation.Default,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
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
                    onClick = {
                        recommendedPracticeId?.let { id -> onStartPractice(id) }
                    },
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
    val icon: ImageVector
)
