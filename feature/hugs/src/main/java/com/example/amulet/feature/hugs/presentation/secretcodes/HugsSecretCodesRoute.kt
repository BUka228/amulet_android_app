package com.example.amulet.feature.hugs.presentation.secretcodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.card.AmuletCard
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.shared.core.AppError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HugsSecretCodesRoute(
    onNavigateBack: () -> Unit = {},
    onOpenPatternDetails: (String) -> Unit = {},
    viewModel: HugsSecretCodesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scaffoldState = LocalScaffoldState.current

    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Секретные коды") },
                        navigationIcon = {
                            TextButton(onClick = onNavigateBack) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                    )
                },
                floatingActionButton = {}
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HugsSecretCodesEffect.ShowError -> {
                    // отображаем через карточку ошибки ниже
                }
                is HugsSecretCodesEffect.OpenPatternDetails -> {
                    onOpenPatternDetails(effect.patternId)
                }
            }
        }
    }

    HugsSecretCodesScreen(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun HugsSecretCodesScreen(
    state: HugsSecretCodesState,
    onIntent: (HugsSecretCodesIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Секретные коды",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.codes, key = { it.id.value }) { pattern ->
                SecretCodeItem(pattern = pattern) {
                    onIntent(HugsSecretCodesIntent.OpenCode(pattern.id.value))
                }
            }
        }

        state.error?.let { error ->
            SecretCodesErrorCard(error = error)
        }
    }
}

@Composable
private fun SecretCodeItem(
    pattern: com.example.amulet.shared.domain.patterns.model.Pattern,
    onClick: () -> Unit,
) {
    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = pattern.title, style = MaterialTheme.typography.bodyLarge)
            pattern.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (pattern.tags.isNotEmpty()) {
                Text(
                    text = pattern.tags.joinToString(prefix = "Теги: "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SecretCodesErrorCard(error: AppError) {
    AmuletCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "Не удалось загрузить секретные коды")
            Text(text = error.toString())
        }
    }
}
