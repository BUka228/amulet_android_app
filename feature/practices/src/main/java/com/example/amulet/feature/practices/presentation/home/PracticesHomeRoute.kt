package com.example.amulet.feature.practices.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PracticesHomeRoute(
    onOpenPractice: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    viewModel: PracticesHomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {

        Spacer(Modifier.height(8.dp))

        val tabs = PracticesTab.entries.toTypedArray()
        TabRow(selectedTabIndex = state.selectedTab.ordinal) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.handleEvent(PracticesHomeEvent.SelectTab(tab)) },
                    text = { Text(if (tab == PracticesTab.Overview) "Обзор" else if (tab == PracticesTab.Courses) "Курсы" else "Избранное") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (state.selectedTab) {
            PracticesTab.Overview -> OverviewTab(state, onOpenPractice, viewModel)
            PracticesTab.Courses -> CoursesTab(state, onOpenCourse)
            PracticesTab.Favorites -> FavoritesTab(state, onOpenPractice)
        }
    }
}

@Composable
private fun OverviewTab(
    state: PracticesHomeState,
    onOpenPractice: (String) -> Unit,
    viewModel: PracticesHomeViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        state.activeSession?.let { s ->
            Text("Активная сессия: ${s.practiceId}", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.handleEvent(PracticesHomeEvent.PauseSession(s.id)) }) { Text("Пауза") }
                Button(onClick = { viewModel.handleEvent(PracticesHomeEvent.ResumeSession(s.id)) }) { Text("Резюме") }
                Button(onClick = { viewModel.handleEvent(PracticesHomeEvent.StopSession(s.id, true)) }) { Text("Стоп") }
            }
            Spacer(Modifier.height(8.dp))
        }
        if (state.categories.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.categories.take(8).forEach { cat ->
                    FilterChip(
                        selected = state.filters.categoryId == cat.id,
                        onClick = { viewModel.handleEvent(PracticesHomeEvent.SelectCategory(cat.id)) },
                        label = { Text(cat.title) }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Рекомендации", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(state.recommendations) { p ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(p.title)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpenPractice(p.id) }) { Text("Открыть") }
                        Button(onClick = { viewModel.handleEvent(PracticesHomeEvent.StartPractice(p.id)) }) { Text("Старт") }
                    }
                }
            }
            if (state.recent.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); Text("Недавние", style = MaterialTheme.typography.titleMedium) }
                items(state.recent) { p ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.title)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onOpenPractice(p.id) }) { Text("Открыть") }
                            Button(onClick = { viewModel.handleEvent(PracticesHomeEvent.StartPractice(p.id)) }) { Text("Старт") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesTab(
    state: PracticesHomeState,
    onOpenCourse: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        if (state.continueCourses.isNotEmpty()) {
            item { Text("Продолжить обучение", style = MaterialTheme.typography.titleMedium) }
            items(state.continueCourses) { c ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(c.title, style = MaterialTheme.typography.titleMedium)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpenCourse(c.id) }) { Text("Продолжить") }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
        item { Text("Каталог курсов", style = MaterialTheme.typography.titleMedium) }
        items(state.courses) { c ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(c.title, style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onOpenCourse(c.id) }) { Text("Открыть") }
                }
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    state: PracticesHomeState,
    onOpenPractice: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.favorites) { p ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.title)
                Button(onClick = { onOpenPractice(p.id) }) { Text("Открыть") }
            }
        }
    }
}
