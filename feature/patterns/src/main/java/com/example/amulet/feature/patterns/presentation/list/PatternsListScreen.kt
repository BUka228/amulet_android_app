package com.example.amulet.feature.patterns.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.components.textfield.AmuletTextField
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.PatternCard
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                        title = { Text(stringResource(R.string.screen_patterns_list)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_navigate_back)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { onEvent(PatternsListEvent.ToggleFilters) }) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.cd_filter_patterns)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { onEvent(PatternsListEvent.CreatePatternClicked) }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_create_pattern)
                        )
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
                                PatternTab.MY_PATTERNS -> stringResource(R.string.tab_my_patterns)
                                PatternTab.PUBLIC -> stringResource(R.string.tab_public_patterns)
                                PatternTab.PRESETS -> stringResource(R.string.tab_presets)
                            }
                        )
                    }
                )
            }
        }

        // Поисковая строка
        var searchText by remember { mutableStateOf(state.searchQuery) }
        
        LaunchedEffect(searchText) {
            snapshotFlow { searchText }
                .debounce(300)
                .collect { query ->
                    onEvent(PatternsListEvent.UpdateSearchQuery(query))
                }
        }
        
        AmuletTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = stringResource(R.string.search_patterns_placeholder),
            leadingIcon = Icons.Default.Search,
            trailingIconContent = if (searchText.isNotEmpty()) {
                {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(R.string.cd_clear_search)
                        )
                    }
                }
            } else null,
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

                if (filteredPatterns.isEmpty() && state.searchQuery.isNotEmpty()) {
                    EmptySearchState(
                        searchQuery = state.searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = filteredPatterns,
                            key = { it.id.value }
                        ) { pattern ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ) + scaleIn(
                                    initialScale = 0.9f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                                exit = fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 200,
                                        easing = FastOutSlowInEasing
                                    )
                                ) + scaleOut(
                                    targetScale = 0.9f,
                                    animationSpec = tween(
                                        durationMillis = 200,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            ) {
                                PatternCard(
                                    pattern = pattern,
                                    onClick = { onEvent(PatternsListEvent.PatternClicked(pattern.id.value)) },
                                    onPreview = { onEvent(PatternsListEvent.PreviewPattern(pattern.id.value)) },
                                    onEdit = { onEvent(PatternsListEvent.PatternClicked(pattern.id.value)) },
                                    onDelete = { onEvent(PatternsListEvent.DeletePattern(pattern.id.value)) },
                                    onDuplicate = { onEvent(PatternsListEvent.DuplicatePattern(pattern.id.value)) },
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                )
                            }
                        }
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
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_patterns_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_patterns_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreatePattern) {
            Icon(
                Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.action_create_pattern))
        }
    }
}

@Composable
fun EmptySearchState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_search_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_search_description, searchQuery),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
