package com.example.amulet.feature.patterns.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.presentation.components.PatternCard
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PatternsListRoute(
    viewModel: PatternsListViewModel = hiltViewModel(),
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is PatternsListSideEffect.NavigateToPatternEditor -> {
                    onNavigateToEditor(effect.patternId)
                }
                is PatternsListSideEffect.NavigateToPatternPreview -> {
                    onNavigateToPreview(effect.patternId)
                }
                is PatternsListSideEffect.ShowSnackbar -> {
                    // Handle snackbar
                }
                is PatternsListSideEffect.ShowDeleteConfirmation -> {
                    // Handle delete confirmation
                }
            }
        }
    }

    PatternsListScreen(
        state = uiState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternsListScreen(
    state: PatternsListState,
    onEvent: (PatternsListEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scaffoldState = LocalScaffoldState.current

    // Настройка TopBar и FAB
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    TopAppBar(
                        title = { Text("Паттерны") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Открыть фильтры */ }) {
                                Icon(Icons.Default.FilterList, contentDescription = "Фильтры")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { onEvent(PatternsListEvent.CreatePatternClicked) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Создать паттерн")
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Табы
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            PatternTab.values().forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onEvent(PatternsListEvent.SelectTab(tab)) },
                    text = { 
                        Text(
                            when (tab) {
                                PatternTab.MY_PATTERNS -> "Мои паттерны"
                                PatternTab.PUBLIC -> "Публичные"
                                PatternTab.PRESETS -> "Пресеты"
                            }
                        )
                    }
                )
            }
        }

        // Поисковая строка
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onEvent(PatternsListEvent.UpdateSearchQuery(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Поиск паттернов...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onEvent(PatternsListEvent.UpdateSearchQuery("")) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            singleLine = true
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.isEmpty && state.selectedTab == PatternTab.MY_PATTERNS -> {
                EmptyPatternsState(
                    onCreatePattern = { onEvent(PatternsListEvent.CreatePatternClicked) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                val filteredPatterns = state.patterns.filter { pattern ->
                    (state.selectedFilter == null || pattern.kind == state.selectedFilter) &&
                    (state.searchQuery.isEmpty() || pattern.title.contains(state.searchQuery, ignoreCase = true))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredPatterns,
                        key = { it.id.value }
                    ) { pattern ->
                        PatternCard(
                            pattern = pattern,
                            onClick = { onEvent(PatternsListEvent.PatternClicked(pattern.id.value)) },
                            onPreview = { onEvent(PatternsListEvent.PreviewPattern(pattern.id.value)) },
                            onDelete = { onEvent(PatternsListEvent.DeletePattern(pattern.id.value)) },
                            onDuplicate = { onEvent(PatternsListEvent.DuplicatePattern(pattern.id.value)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPatternsState(
    onCreatePattern: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет паттернов",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте свой первый паттерн для амулета",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreatePattern) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать паттерн")
        }
    }
}
