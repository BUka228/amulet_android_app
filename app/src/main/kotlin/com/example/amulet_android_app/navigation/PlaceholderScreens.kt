package com.example.amulet_android_app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.amulet_android_app.R
import com.example.amulet.core.design.foundation.color.AmuletPalette
import com.example.amulet.core.design.scaffold.LocalScaffoldState

/**
 * Placeholder экраны для bottom navigation items
 * Будут заменены на реальные feature модули
 */

@Composable
fun LibraryPlaceholderScreen() {
    val scaffoldState = LocalScaffoldState.current
    // Обнуляем topBar и FAB, bottomBar управляется в MainScaffold
    SideEffect {
        scaffoldState.updateConfig {
            copy(topBar = {}, floatingActionButton = {})
        }
    }
    
    PlaceholderContent(
        icon = Icons.AutoMirrored.Filled.List,
        titleRes = R.string.placeholder_library_title,
        descriptionRes = R.string.placeholder_library_description
    )
}

@Composable
fun HugsPlaceholderScreen() {
    val scaffoldState = LocalScaffoldState.current
    // Обнуляем topBar и FAB, bottomBar управляется в MainScaffold
    SideEffect {
        scaffoldState.updateConfig {
            copy(topBar = {}, floatingActionButton = {})
        }
    }
    
    PlaceholderContent(
        icon = Icons.Default.Favorite,
        titleRes = R.string.placeholder_hugs_title,
        descriptionRes = R.string.placeholder_hugs_description
    )
}

@Composable
fun PatternsPlaceholderScreen() {
    val scaffoldState = LocalScaffoldState.current
    // Обнуляем topBar и FAB, bottomBar управляется в MainScaffold
    SideEffect {
        scaffoldState.updateConfig {
            copy(topBar = {}, floatingActionButton = {})
        }
    }
    
    PlaceholderContent(
        icon = Icons.Default.Notifications,
        titleRes = R.string.placeholder_patterns_title,
        descriptionRes = R.string.placeholder_patterns_description
    )
}

@Composable
fun SettingsPlaceholderScreen() {
    val scaffoldState = LocalScaffoldState.current
    // Обнуляем topBar и FAB, bottomBar управляется в MainScaffold
    SideEffect {
        scaffoldState.updateConfig {
            copy(topBar = {}, floatingActionButton = {})
        }
    }
    
    PlaceholderContent(
        icon = Icons.Default.Settings,
        titleRes = R.string.placeholder_settings_title,
        descriptionRes = R.string.placeholder_settings_description
    )
}

@Composable
private fun PlaceholderContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    titleRes: Int,
    descriptionRes: Int
) {
    val title = stringResource(titleRes)
    val description = stringResource(descriptionRes)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmuletPalette.Primary,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.placeholder_in_development),
            style = MaterialTheme.typography.labelLarge,
            color = AmuletPalette.Secondary
        )
    }
}
