# GhCliApiClient Logging Guide

## Overview
The `GhCliApiClient` now includes comprehensive logging and debugging support to help troubleshoot issues and understand API request behavior.

## Logging Levels

The client uses the existing `Log` class with different levels:

### VERBOSE (Level 1)
Shows all details including:
- Full command being executed
- Complete request parameters
- Response previews (first 500 characters)
- Process execution timing
- All debug and info messages

Example output:
```
[1] GH CLI API Request: /repos/square/okhttp/pulls/7415
[1] Parameters: {}
[1] Command: gh api /repos/square/okhttp/pulls/7415 --method GET
[1] Starting process execution...
[1] Process completed in 245ms with exit code: 0
[1] Response received in 250ms (1024 bytes)
[1] Response preview: {"id":123,"number":7415,"title":"Fix..."...
```

### DEBUG (Level 2)
Shows request tracking and timing:
- Request endpoints and IDs
- Response times and sizes
- Parsed item counts
- Error summaries

Example output:
```
[1] GH CLI API Request: /repos/square/okhttp/pulls/7415
[1] Response received in 250ms (1024 bytes)
```

### INFO (Level 3)
Shows high-level information:
- Client initialization
- Statistics summaries
- Important state changes

Example output:
```
GhCliApiClient initialized - using GitHub CLI for API requests
```

### WARNING (Level 4)
Shows errors and problems:
- Command failures
- Authentication issues
- Process errors

Example output:
```
[1] gh command failed with exit code 1
[1] Command: gh api /repos/invalid/repo
[1] Output: error: HTTP 404: Not Found
```

## Configuration

Set the log level in your code or `BuildConfig`:

```kotlin
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.logging.Log

// Set to VERBOSE for maximum detail
BuildConfig.logLevel = Log.VERBOSE

// Set to DEBUG for balanced output
BuildConfig.logLevel = Log.DEBUG

// Set to INFO for minimal output
BuildConfig.logLevel = Log.INFO

// Set to NONE to disable logging
BuildConfig.logLevel = Log.NONE
```

## Request Tracking

Each API request is assigned a unique request ID (sequential number) that appears in all related log messages:

```
[1] GH CLI API Request: /repos/owner/repo/pulls
[1] Response received in 145ms (2048 bytes)
[2] GH CLI API List Request: /repos/owner/repo/issues/123/timeline
[2] List response received in 180ms (5120 bytes)
[2] Parsed 25 items from response
```

This makes it easy to correlate all log messages for a specific request.

## Statistics Tracking

The client tracks performance metrics:
- Total number of requests
- Total request time
- Average time per request
- Total data transferred
- Average response size

### Getting Statistics

```kotlin
val client = GhCliApiClient()

// Make some requests...
client.pullRequest("square", "okhttp", 7415)
client.pullRequests("square", "okhttp")

// Log statistics
client.logStatistics()

// Or get as string
val stats = client.getRequestStatistics()
println(stats)
```

Example statistics output:
```
GH CLI API Client Statistics:
  Total Requests: 10
  Total Time: 2450ms
  Average Time: 245ms per request
  Total Data: 0.15 MB
  Average Size: 15728 bytes per response
```

## Debugging Scenarios

### Troubleshooting Slow Requests

Set log level to DEBUG to see timing information:
```kotlin
BuildConfig.logLevel = Log.DEBUG
val client = GhCliApiClient()
// Watch for slow responses in logs
```

### Debugging Authentication Issues

Set log level to VERBOSE to see full command output:
```kotlin
BuildConfig.logLevel = Log.VERBOSE
val client = GhCliApiClient()
// Check for "not authenticated" in command output
```

### Analyzing Response Data

Set log level to VERBOSE to see response previews:
```kotlin
BuildConfig.logLevel = Log.VERBOSE
// Response preview shows first 500 characters of JSON
```

### Checking Command Construction

Set log level to VERBOSE to see exact commands:
```kotlin
BuildConfig.logLevel = Log.VERBOSE
// See: gh api /repos/owner/repo/pulls --method GET -F state=closed
```

## Comparison with RetrofitApiClient

The logging approach is designed to match Retrofit's `HttpLoggingInterceptor`:

| Feature | Retrofit | GH CLI |
|---------|----------|--------|
| Request logging | ✓ | ✓ |
| Response logging | ✓ | ✓ |
| Timing information | ✓ | ✓ |
| Request IDs | - | ✓ |
| Statistics tracking | - | ✓ |
| Command visibility | N/A | ✓ |

## Best Practices

1. **Development**: Use `Log.VERBOSE` or `Log.DEBUG` for detailed troubleshooting
2. **Production**: Use `Log.INFO` or `Log.WARNING` to reduce noise
3. **CI/CD**: Use `Log.WARNING` to only see errors
4. **Performance Testing**: Enable DEBUG level and use `logStatistics()` to track metrics

## Example Usage

```kotlin
import dev.hossain.githubstats.BuildConfig
import dev.hossain.githubstats.client.GhCliApiClient
import dev.hossain.githubstats.logging.Log
import kotlinx.coroutines.runBlocking

fun main() {
    // Enable verbose logging
    BuildConfig.logLevel = Log.VERBOSE
    
    val client = GhCliApiClient()
    
    runBlocking {
        // Make some API calls
        val pr = client.pullRequest("square", "okhttp", 7415)
        println("Fetched PR: ${pr.title}")
        
        val prs = client.pullRequests("square", "okhttp", size = 10)
        println("Fetched ${prs.size} pull requests")
    }
    
    // Show statistics
    client.logStatistics()
}
```

## Troubleshooting

### No logs appearing
- Check that `BuildConfig.logLevel` is set appropriately
- Ensure you're looking at the correct output stream (stdout)

### Too much logging
- Increase log level: `BuildConfig.logLevel = Log.INFO`
- Disable verbose mode in production

### Missing timing information
- Set log level to at least DEBUG: `BuildConfig.logLevel = Log.DEBUG`

### Can't see command details
- Set log level to VERBOSE: `BuildConfig.logLevel = Log.VERBOSE`

## See Also

- [Log.kt](../logging/Log.kt) - Logging framework
- [GhCliApiClient.kt](GhCliApiClient.kt) - Implementation
- [BuildConfig.kt](../BuildConfig.kt) - Log level configuration
