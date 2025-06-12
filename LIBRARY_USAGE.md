# GitHub Stats - Library Usage Guide

This document describes how to use the GitHub Stats project as a standalone library in your applications.

## Overview

The GitHub Stats library allows you to generate GitHub pull request review statistics for any repository and users. It can be used in two ways:

1. **As a Fat JAR** - Complete standalone executable with all dependencies
2. **As a Library JAR** - Include in your project dependencies

## Build Artifacts

Run the following command to build both JARs:

```bash
./gradlew build
```

This generates:
- `build/libs/github-stats-1.0-SNAPSHOT.jar` - Library JAR (dependencies not included)
- `build/libs/github-stats-standalone-1.0-SNAPSHOT.jar` - Fat JAR (all dependencies included)

## Usage Options

### Option 1: Using the Fat JAR (Standalone Executable)

The fat JAR contains all dependencies and can be run directly with Java:

```bash
java -jar github-stats-standalone-1.0-SNAPSHOT.jar
```

**Requirements:**
- Java 17 or higher
- Create a `local.properties` file in the working directory (see Configuration section)

### Option 2: Using as a Library in Your Project

#### Gradle

Add the JAR to your project's `libs` folder and include it as a dependency:

```kotlin
dependencies {
    implementation(files("libs/github-stats-1.0-SNAPSHOT.jar"))
    
    // Required runtime dependencies (if not already included)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("io.insert-koin:koin-core:4.1.0")
    // ... other dependencies as needed
}
```

#### Maven

```xml
<dependency>
    <groupId>dev.hossain.githubstats</groupId>
    <artifactId>github-stats</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/github-stats-1.0-SNAPSHOT.jar</systemPath>
</dependency>
```

### Option 3: Using the Library API

Use the `GitHubStatsLibrary` class for programmatic access:

```kotlin
import dev.hossain.githubstats.GitHubStatsConfig
import dev.hossain.githubstats.GitHubStatsLibrary
import dev.hossain.githubstats.logging.Log

fun main() {
    // Configure the library
    val config = GitHubStatsConfig(
        githubToken = "your_github_token_here",
        repoOwner = "freeCodeCamp",
        repoName = "freeCodeCamp", 
        userIds = listOf("naomi-lgbt", "RandellDawson"),
        dateAfter = "2023-01-01",
        dateBefore = "2023-12-31",
        logLevel = Log.INFO
    )

    // Initialize and use the library
    val statsLibrary = GitHubStatsLibrary()
    
    try {
        statsLibrary.initialize(config)
        
        // Generate stats
        val results = statsLibrary.generateAllStats()
        results.forEach { println(it) }
        
    } finally {
        statsLibrary.shutdown()
    }
}
```

## Configuration

### For Fat JAR Usage

Create a `local.properties` file in your working directory:

```properties
# GitHub API Token (required)
github.token=your_github_token_here

# Repository information (required)
github.repo.owner=freeCodeCamp
github.repo.name=freeCodeCamp

# Users to generate stats for (required)
github.user.1=naomi-lgbt
github.user.2=RandellDawson

# Date range filters (optional)
date.limit.after=2023-01-01
date.limit.before=2023-12-31

# Bot users to exclude from stats (optional)
github.bot.user.ids=dependabot[bot],github-actions[bot]
```

### For Library API Usage

Use the `GitHubStatsConfig` data class:

```kotlin
val config = GitHubStatsConfig(
    githubToken = "your_github_token_here",     // Required
    repoOwner = "freeCodeCamp",                 // Required  
    repoName = "freeCodeCamp",                  // Required
    userIds = listOf("naomi-lgbt"),             // Required
    dateAfter = "2023-01-01",                   // Optional
    dateBefore = "2023-12-31",                  // Optional
    botUserIds = listOf("dependabot[bot]"),     // Optional
    logLevel = Log.INFO                         // Optional
)
```

## API Reference

### GitHubStatsLibrary Class

Main library facade providing the following methods:

#### `initialize(config: GitHubStatsConfig)`
Initialize the library with configuration. Must be called before other methods.

#### `generateAuthorStats(): List<String>`
Generate statistics for PRs created by the configured users.

#### `generateReviewerStats(): List<String>` 
Generate statistics for PRs reviewed by the configured users.

#### `generateAllStats(): List<String>`
Generate both author and reviewer statistics.

#### `getAuthorStatsData(authorUserId: String): AuthorStats`
Get raw author statistics data without formatting (suspend function).

#### `getReviewerStatsData(reviewerUserId: String): ReviewerReviewStats`
Get raw reviewer statistics data without formatting (suspend function).

#### `shutdown()`
Clean up resources. Call when finished using the library.

### GitHubStatsConfig Data Class

Configuration parameters:

- `githubToken: String` - GitHub personal access token (required)
- `repoOwner: String` - Repository owner username/organization (required)
- `repoName: String` - Repository name (required) 
- `userIds: List<String>` - GitHub usernames to analyze (required)
- `dateAfter: String?` - Start date filter in ISO format YYYY-MM-DD (optional)
- `dateBefore: String?` - End date filter in ISO format YYYY-MM-DD (optional)
- `botUserIds: List<String>?` - Bot usernames to exclude from analysis (optional)
- `logLevel: Int` - Logging level (default: Log.INFO, optional)

## Output

The library generates statistics in multiple formats:

1. **ASCII Tables** - Human-readable console output
2. **CSV Files** - Structured data files for analysis
3. **Raw Data Objects** - For programmatic processing

Files are generated in the working directory:
- `*-author-stats.csv` - PR author statistics
- `*-reviewer-stats.csv` - PR reviewer statistics  
- `*-aggregated-stats.csv` - Combined statistics

## Requirements

- Java 17 or higher
- GitHub personal access token with repository access
- Internet connection for GitHub API access

## Error Handling

The library handles common errors gracefully:

- Invalid GitHub tokens
- API rate limiting  
- Network connectivity issues
- Missing or invalid configuration

Check log output for detailed error information.

## Examples

See `src/main/kotlin/dev/hossain/githubstats/example/LibraryUsageExample.kt` for a complete working example.

## Limitations

- GitHub API rate limits apply (5,000 requests per hour for authenticated users)
- Large repositories may take significant time to analyze
- Currently supports single repository analysis per session
- Time zone handling is limited to North America

## Support

For issues and questions, please check the project repository's issue tracker.