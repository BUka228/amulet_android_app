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
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.core.design.scaffold.ConfigureTopBar
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    val spacing = AmuletTheme.spacing
    val scrollState = rememberScrollState()
    val scaffoldState = LocalScaffoldState.current

    // Настраиваем TopBar через централизованный конфиг
    // Используем userName как ключ для переустановки при изменениях и restoreState
    scaffoldState.ConfigureTopBar(uiState.userName) {
        DashboardTopBar(
            userName = uiState.userName,
            onSettingsClick = { onEvent(DashboardUiEvent.NavigateToSettings) },
            onRefreshClick = { onEvent(DashboardUiEvent.Refresh) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = spacing.md)
            .padding(bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Spacer(modifier = Modifier.height(spacing.xs))
        
        // Компактная статистика в верхней части
        CompactStatsRow(
            stats = uiState.dailyStats
        )

        // Устройства - компактная версия
        DevicesSection(
            devices = uiState.devices,
            connectedDevice = uiState.connectedDevice,
            onDeviceClick = { deviceId -> onEvent(DashboardUiEvent.DeviceClicked(deviceId)) },
            onNavigateToPairing = { onEvent(DashboardUiEvent.NavigateToPairing) },
            onNavigateToDevicesList = { onEvent(DashboardUiEvent.NavigateToDevicesList) }
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
        
        // Быстрый доступ к разделам
        QuickAccessGrid(
            onNavigateToLibrary = { onEvent(DashboardUiEvent.NavigateToLibrary) },
            onNavigateToHugs = { onEvent(DashboardUiEvent.NavigateToHugs) },
            onNavigateToPatterns = { onEvent(DashboardUiEvent.NavigateToPatterns) }
        )

        Spacer(modifier = Modifier.height(spacing.sm))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    userName: String?,
    onSettingsClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    val spacing = AmuletTheme.spacing
    
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
            IconButton(onClick = onRefreshClick) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.dashboard_action_refresh)
                )
            }
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
