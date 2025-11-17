package com.example.amulet.feature.patterns.presentation.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.amulet.core.design.scaffold.LocalScaffoldState
import com.example.amulet.feature.patterns.R
import com.example.amulet.feature.patterns.presentation.components.FilterBottomSheet
import com.example.amulet.feature.patterns.presentation.components.PatternCard
import com.example.amulet.feature.patterns.presentation.components.PatternDetailsBottomSheet
import com.example.amulet.feature.patterns.presentation.components.SelectedTagsRow
import com.example.amulet.shared.core.AppError
import com.example.amulet.shared.domain.patterns.model.Pattern
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun PatternsListRoute(
    viewModel: PatternsListViewModel = hiltViewModel(),
    onNavigateToEditor: (String?) -> Unit,
    onNavigateToPreview: (String) -> Unit,
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
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun PatternsListScreen(
    state: PatternsListState,
    onEvent: (PatternsListEvent) -> Unit,
) {
    val scaffoldState = LocalScaffoldState.current
    var searchText by remember { mutableStateOf(state.searchQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedPatternForDetails by remember { mutableStateOf<Pattern?>(null) }

    // Настройка TopBar и FAB
    SideEffect {
        scaffoldState.updateConfig {
            copy(
                topBar = {
                    if (state.isSearchActive) {
                        SearchTopBar(
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            onClose = {
                                searchText = ""
                                onEvent(PatternsListEvent.ToggleSearch)
                            },
                            onClear = { searchText = "" },
                            keyboardController = keyboardController
                        )
                    } else {
                        TopAppBar(
                            title = { Text(stringResource(R.string.screen_patterns_list)) },
                            actions = {
                                IconButton(
                                    onClick = { onEvent(PatternsListEvent.ToggleFilterSheet) }
                                ) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Фильтры"
                                    )
                                }
                                IconButton(
                                    onClick = { onEvent(PatternsListEvent.ToggleSearch) }
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = stringResource(R.string.cd_search_patterns)
                                    )
                                }
                            }
                        )
                    }
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

    // Обновление поискового запроса с debounce
    LaunchedEffect(searchText) {
        snapshotFlow { searchText }
            .debounce(300)
            .collect { query ->
                onEvent(PatternsListEvent.UpdateSearchQuery(query))
            }
    }

    // Bottom sheet с деталями паттерна
    selectedPatternForDetails?.let { pattern ->
        PatternDetailsBottomSheet(
            pattern = pattern,
            onDismiss = { selectedPatternForDetails = null },
            onTagClick = { tag -> onEvent(PatternsListEvent.AddTagFilter(tag)) }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            PatternTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onEvent(PatternsListEvent.SelectTab(tab)) },
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        ),
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

        // Строка с выбранными тегами фильтрации
        SelectedTagsRow(
            selectedTags = state.selectedTags,
            onTagClick = { tag -> onEvent(PatternsListEvent.RemoveTagFilter(tag)) },
            onClearAll = { onEvent(PatternsListEvent.ClearTagFilters) }
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

            state.myPatterns.isEmpty() && state.selectedTab == PatternTab.MY_PATTERNS -> {
                EmptyPatternsState(
                    onCreatePattern = { onEvent(PatternsListEvent.CreatePatternClicked) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                val filteredPatterns = state.patterns.filter { pattern ->
                    val matchesSearch = state.searchQuery.isEmpty() || pattern.title.contains(state.searchQuery, ignoreCase = true)
                    val matchesKind = state.selectedKinds.isEmpty() || pattern.kind in state.selectedKinds
                    val matchesTags = state.selectedTags.isEmpty() || pattern.tags.any { it in state.selectedTags }
                    matchesSearch && matchesKind && matchesTags
                }

                if (filteredPatterns.isEmpty() && (state.searchQuery.isNotEmpty() || state.selectedKinds.isNotEmpty() || state.selectedTags.isNotEmpty())) {
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
                            PatternCard(
                                pattern = pattern,
                                onPreview = { onEvent(PatternsListEvent.PreviewPattern(pattern.id.value)) },
                                onEdit = { onEvent(PatternsListEvent.PatternClicked(pattern.id.value)) },
                                onDelete = { onEvent(PatternsListEvent.DeletePattern(pattern.id.value)) },
                                onShowDetails = { selectedPatternForDetails = pattern },
                                onTagClick = { tag -> onEvent(PatternsListEvent.AddTagFilter(tag)) }
                            )
                        }
                    }
                }
            }
        }
        
        // Bottom Sheet для фильтрации
        if (state.isFilterSheetVisible) {
            FilterBottomSheet(
                selectedKinds = state.selectedKinds,
                selectedTags = state.selectedTags,
                availableTags = state.availableTags,
                onKindToggle = { kind -> onEvent(PatternsListEvent.ToggleKindFilter(kind)) },
                onTagClick = { tag -> onEvent(PatternsListEvent.AddTagFilter(tag)) },
                onClearFilters = { onEvent(PatternsListEvent.ClearFilters) },
                onDismiss = { onEvent(PatternsListEvent.HideFilterSheet) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    val focusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
        onDispose { }
    }

    TopAppBar(
        title = {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        stringResource(R.string.search_patterns_placeholder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_close_search)
                )
            }
        },
        actions = {
            if (searchText.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        }
    )
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
