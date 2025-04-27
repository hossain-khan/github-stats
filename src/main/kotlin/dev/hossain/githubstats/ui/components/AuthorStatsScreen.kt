package dev.hossain.githubstats.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.hossain.githubstats.AuthorStats
import dev.hossain.githubstats.ui.util.Duration
import dev.hossain.githubstats.ui.viewmodel.GitHubStatsViewModel

/**
 * Screen for displaying PR author statistics.
 */
@Composable
fun AuthorStatsScreen(viewModel: GitHubStatsViewModel) {
    val authorStats by viewModel.authorStats.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                "PR Author Statistics",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = { viewModel.generateAuthorStats() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Generate")
                Spacer(Modifier.width(4.dp))
                Text("Generate Stats")
            }
        }
        
        if (authorStats.isEmpty()) {
            // Empty state
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No author stats available yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Configure your settings and click 'Generate Stats' to analyze PR author data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Display stats
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(authorStats) { stats ->
                    AuthorStatsCard(stats)
                }
            }
        }
    }
}

/**
 * Card displaying stats for a specific author.
 */
@Composable
fun AuthorStatsCard(stats: AuthorStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Author info header
            Text(
                "Stats for ${stats.authorId}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Summary stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                StatItem(
                    label = "Total PRs",
                    value = stats.prStats.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Avg. Time to Merge",
                    value = formatDuration(stats.averageMergeTime),
                    modifier = Modifier.weight(1.5f)
                )
                
                StatItem(
                    label = "Unique Reviewers",
                    value = stats.reviewers.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Reviewer stats
            Text(
                "Reviewer Statistics",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            stats.reviewers.forEach { (reviewer, reviewCount) ->
                ReviewerStatRow(
                    reviewer = reviewer,
                    reviewCount = reviewCount,
                    avgTime = stats.reviewStats[reviewer]?.averageReviewTime ?: Duration.ZERO
                )
            }
        }
    }
}

/**
 * Row displaying stats for a specific reviewer.
 */
@Composable
fun ReviewerStatRow(
    reviewer: String,
    reviewCount: Int,
    avgTime: Duration
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            reviewer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            "$reviewCount reviews",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )
        
        Text(
            formatDuration(avgTime),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Simple stat display component.
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format a duration for display.
 */
private fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.toSeconds()
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    
    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m")
        if (isEmpty()) append("< 1m")
    }.trim()
}