package com.example.amulet.feature.hugs.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.avatar.AmuletAvatar
import com.example.amulet.core.design.components.avatar.AvatarSize
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.hugs.R
import com.example.amulet.shared.domain.hugs.model.Hug
import com.example.amulet.shared.domain.hugs.model.HugStatus
import com.example.amulet.shared.domain.hugs.model.PairStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsRoute(
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenEmotions: () -> Unit = {},
    onOpenSecretCodes: () -> Unit = {},
    onOpenPairing: () -> Unit = {},
    viewModel: HugsHomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HugsHomeEffect.NavigateToHistory -> onOpenHistory()
                HugsHomeEffect.NavigateToSettings -> onOpenSettings()
                HugsHomeEffect.NavigateToEmotions -> onOpenEmotions()
                HugsHomeEffect.NavigateToSecretCodes -> onOpenSecretCodes()
                HugsHomeEffect.NavigateToPairing -> onOpenPairing()
                is HugsHomeEffect.ShowError -> {
                    // TODO: показать ошибку через Snackbar, когда появится инфраструктура
                }
            }
        }
    }

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.hugs_home_title), style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            IconButton(onClick = { viewModel.onIntent(HugsHomeIntent.OpenHistory) }) {
                                Icon(imageVector = Icons.Filled.History, contentDescription = null)
                            }
                            IconButton(onClick = { viewModel.onIntent(HugsHomeIntent.OpenEmotions) }) {
                                Icon(imageVector = Icons.Filled.Favorite, contentDescription = null)
                            }
                            IconButton(onClick = { viewModel.onIntent(HugsHomeIntent.OpenSettings) }) {
                                Icon(imageVector = Icons.Filled.Settings, contentDescription = null)
                            }
                        }
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing || state.isLoading,
        onRefresh = { viewModel.onIntent(HugsHomeIntent.Refresh) },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        HugsHomeScreen(
            state = state,
            onIntent = viewModel::onIntent,
        )
    }
}

@Composable
private fun HugsHomeScreen(
    state: HugsHomeState,
    onIntent: (HugsHomeIntent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            PairHeaderSection(state = state, onIntent = onIntent)
        }

        item {
            QuickActionsSection(onIntent = onIntent)
        }

        if (state.hugs.isNotEmpty()) {
            item {
                RecentHugsSection(hugs = state.hugs)
            }
        }
    }
}

@Composable
private fun PairHeaderSection(
    state: HugsHomeState,
    onIntent: (HugsHomeIntent) -> Unit,
) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.hugs_home_pair_section_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            val pair = state.activePair
            val hasPair = pair != null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PairMemberCard(
                    title = state.currentUser?.displayName ?: stringResource(R.string.hugs_home_you),
                    subtitle = stringResource(R.string.hugs_home_you),
                    highlight = true,
                    avatarUrl = state.currentUser?.avatarUrl,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                val partnerSubtitle = when (pair?.status) {
                    PairStatus.ACTIVE -> stringResource(R.string.hugs_home_partner_active)
                    PairStatus.PENDING -> stringResource(R.string.hugs_home_partner_pending)
                    PairStatus.BLOCKED -> stringResource(R.string.hugs_home_partner_blocked)
                    null -> stringResource(R.string.hugs_home_partner_not_configured)
                }

                PairMemberCard(
                    title = state.partnerUser?.displayName
                        ?: if (hasPair) stringResource(R.string.hugs_home_partner_title) else stringResource(R.string.hugs_home_no_partner_title),
                    subtitle = partnerSubtitle,
                    highlight = hasPair,
                    avatarUrl = state.partnerUser?.avatarUrl,
                    modifier = Modifier.weight(1f)
                )
            }

            if (hasPair) {
                Text(
                    text = stringResource(R.string.hugs_home_pair_active_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(R.string.hugs_home_pair_inactive_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = { onIntent(HugsHomeIntent.OpenPairing) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.hugs_home_setup_pair_button))
                }
            }

            Button(
                onClick = { onIntent(HugsHomeIntent.SendHug) },
                enabled = hasPair && !state.isSending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (state.isSending) stringResource(R.string.hugs_home_send_in_progress) else stringResource(R.string.hugs_home_send_button),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onIntent: (HugsHomeIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionCard(
                title = stringResource(R.string.hugs_home_quick_actions_history_title),
                description = stringResource(R.string.hugs_home_quick_actions_history_desc),
                icon = Icons.Filled.History,
                onClick = { onIntent(HugsHomeIntent.OpenHistory) },
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                title = stringResource(R.string.hugs_home_quick_actions_emotions_title),
                description = stringResource(R.string.hugs_home_quick_actions_emotions_desc),
                icon = Icons.Filled.Favorite,
                onClick = { onIntent(HugsHomeIntent.OpenEmotions) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            ActionCard(
                title = stringResource(R.string.hugs_home_quick_actions_codes_title),
                description = stringResource(R.string.hugs_home_quick_actions_codes_desc),
                icon = Icons.Filled.Settings,
                onClick = { onIntent(HugsHomeIntent.OpenSecretCodes) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentHugsSection(hugs: List<Hug>) {
    AmuletCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.hugs_home_recent_hugs_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (hugs.isEmpty()) {
                Text(
                    text = stringResource(R.string.hugs_home_recent_hugs_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    hugs.forEach { hug ->
                        RecentHugItem(hug = hug)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentHugItem(hug: Hug) {
    val emotionColor = remember(hug.emotion.colorHex) {
        parseEmotionColor(hug.emotion.colorHex)
    }
    val formattedDateTime = remember(hug.createdAt) {
        formatHugDateTime(hug)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(emotionColor)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when (hug.status) {
                    HugStatus.SENT -> stringResource(R.string.hugs_home_status_sent)
                    HugStatus.DELIVERED -> stringResource(R.string.hugs_home_status_delivered)
                    HugStatus.READ -> stringResource(R.string.hugs_home_status_read)
                    HugStatus.EXPIRED -> stringResource(R.string.hugs_home_status_expired)
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formattedDateTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PairMemberCard(
    title: String,
    subtitle: String,
    highlight: Boolean,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (avatarUrl != null && avatarUrl.isNotBlank()) {
            AmuletAvatar(
                imageUrl = avatarUrl,
                initials = title.trim().takeIf { it.isNotBlank() },
                size = if (highlight) AvatarSize.ExtraLarge else AvatarSize.Medium,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(if (highlight) 72.dp else 56.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun parseEmotionColor(hex: String?): Color {
    if (hex.isNullOrBlank()) return Color.Gray
    return try {
        val clean = hex.removePrefix("#")
        val r = clean.take(2).toInt(16)
        val g = clean.substring(2, 4).toInt(16)
        val b = clean.substring(4, 6).toInt(16)
        Color(r, g, b)
    } catch (_: Exception) {
        Color.Gray
    }
}

private fun formatHugDateTime(hug: Hug): String {
    val dt = hug.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dt.date.day.toString().padStart(2, '0')
    val month = (dt.date.month.ordinal + 1).toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$day.$month $hour:$minute"
}
