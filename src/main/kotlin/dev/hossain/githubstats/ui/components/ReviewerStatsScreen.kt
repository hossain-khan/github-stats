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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.hossain.githubstats.ReviewStats
import dev.hossain.githubstats.ui.util.Duration
import dev.hossain.githubstats.ui.viewmodel.GitHubStatsViewModel

/**
 * Screen for displaying PR reviewer statistics.
 */
@Composable
fun ReviewerStatsScreen(viewModel: GitHubStatsViewModel) {
    val reviewerStats by viewModel.reviewerStats.collectAsState()
    
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
                "PR Reviewer Statistics",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = { viewModel.generateReviewerStats() }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Generate")
                Spacer(Modifier.width(4.dp))
                Text("Generate Stats")
            }
        }
        
        if (reviewerStats.isEmpty()) {
            // Empty state
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No reviewer stats available yet",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Configure your settings and click 'Generate Stats' to analyze PR reviewer data",
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
                items(reviewerStats) { stats ->
                    ReviewerStatsCard(stats)
                }
            }
        }
    }
}

/**
 * Card displaying stats for a specific reviewer.
 */
@Composable
fun ReviewerStatsCard(stats: ReviewStats) {
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
            // Reviewer info header
            Text(
                "Stats for ${stats.reviewerId}",
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
                    label = "Total Reviews",
                    value = stats.totalReviews.toString(),
                    modifier = Modifier.weight(1f)
                )
                
                StatItem(
                    label = "Avg. Review Time",
                    value = formatDuration(stats.averageReviewTime),
                    modifier = Modifier.weight(1.5f)
                )
                
                StatItem(
                    label = "Authors Reviewed",
                    value = stats.reviewedPrAuthors.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Author stats
            Text(
                "PRs Reviewed by Author",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (stats.reviewedPrAuthors.isNotEmpty()) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                ) {
                    Text(
                        "Author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        "PRs Reviewed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(100.dp)
                    )
                }
                
                // Author rows
                stats.reviewedPrAuthors.forEach { (author, count) ->
                    AuthorReviewRow(author = author, reviewCount = count)
                }
            } else {
                Text(
                    "No author data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Row displaying stats for a specific author that was reviewed.
 */
@Composable
fun AuthorReviewRow(
    author: String,
    reviewCount: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            author,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            "$reviewCount",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )
    }
}