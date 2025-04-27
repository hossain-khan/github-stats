package dev.hossain.githubstats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hossain.githubstats.ui.viewmodel.GitHubStatsViewModel
import org.jetbrains.compose.components.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.components.splitpane.HorizontalSplitPane
import org.jetbrains.compose.components.splitpane.rememberSplitPaneState

/**
 * Main UI component for the GitHub Stats app.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSplitPaneApi::class)
@Composable
fun GithubStatsApp() {
    val viewModel = remember { GitHubStatsViewModel() }
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedTabIndex = remember { mutableStateOf(0) }
    val logMessages by viewModel.logMessages.collectAsState()
    val splitPaneState = rememberSplitPaneState(0.7f)
    
    // Load config on first launch
    LaunchedEffect(Unit) {
        viewModel.loadConfigFromFile()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GitHub PR Stats") },
                actions = {
                    IconButton(onClick = { selectedTabIndex.value = 2 }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = { /* Show about dialog */ }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab navigation
            TabRow(selectedTabIndex = selectedTabIndex.value) {
                Tab(
                    selected = selectedTabIndex.value == 0,
                    onClick = { selectedTabIndex.value = 0 },
                    text = { Text("Author Stats") }
                )
                Tab(
                    selected = selectedTabIndex.value == 1,
                    onClick = { selectedTabIndex.value = 1 },
                    text = { Text("Reviewer Stats") }
                )
                Tab(
                    selected = selectedTabIndex.value == 2,
                    onClick = { selectedTabIndex.value = 2 },
                    text = { Text("Settings") }
                )
            }
            
            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            // Main content with split pane
            HorizontalSplitPane(splitPaneState = splitPaneState) {
                first {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        when (selectedTabIndex.value) {
                            0 -> AuthorStatsScreen(viewModel)
                            1 -> ReviewerStatsScreen(viewModel)
                            2 -> ConfigScreen(viewModel)
                        }
                    }
                }
                second {
                    LogConsole(logMessages)
                }
            }
        }
    }
}

/**
 * Console-like component to display log messages.
 */
@Composable
fun LogConsole(logMessages: List<String>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "Log Console",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                val listState = rememberLazyListState()
                
                LaunchedEffect(logMessages.size) {
                    if (logMessages.isNotEmpty()) {
                        listState.animateScrollToItem(logMessages.size - 1)
                    }
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(logMessages.size) { index ->
                        Text(
                            text = logMessages[index],
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}