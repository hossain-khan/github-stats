# API Client Implementations Guide

This guide provides comprehensive documentation for the GitHub Stats API client implementations, helping you choose and configure the right client for your needs.

## Overview

GitHub Stats supports two pluggable API client implementations:

| Client | Description | Best For |
|--------|-------------|----------|
| **Retrofit/OkHttp** | HTTP-based client with built-in caching | Production, CI/CD, high-volume usage |
| **GitHub CLI** | Shell-based client using `gh` command | Development, testing, quick local runs |

Both implementations share the same `GitHubApiClient` interface and return identical data structures, making them interchangeable.

## Quick Comparison

| Metric | Retrofit/OkHttp | GitHub CLI |
|--------|-----------------|------------|
| **Avg Response Time** | ~145ms | ~285ms |
| **Cache Support** | HTTP + Database | Database only |
| **Setup Complexity** | Medium | Low |
| **Rate Limit Impact** | Lower (cached) | Higher (no HTTP cache) |
| **Authentication** | Personal Access Token | `gh auth login` |

---

## Retrofit/OkHttp (Default)

The default implementation uses [Retrofit](https://square.github.io/retrofit/) with [OkHttp](https://square.github.io/okhttp/) for making GitHub API requests.

### Advantages

| Advantage | Description |
|-----------|-------------|
| ✅ **Built-in HTTP Caching** | OkHttp automatically caches responses based on HTTP headers, reducing duplicate API calls |
| ✅ **Database Cache Support** | Optional PostgreSQL caching for persistent storage across runs |
| ✅ **Better Performance** | Direct HTTP connections with connection pooling (~145ms avg response time) |
| ✅ **Rich Debugging** | HTTP interceptors provide detailed request/response logging |
| ✅ **Type-safe API Calls** | Retrofit's annotation-based interface ensures compile-time safety |
| ✅ **Efficient Concurrency** | Connection pooling enables efficient parallel requests |

### Disadvantages

| Disadvantage | Description |
|--------------|-------------|
| ❌ **Token Management** | Requires manual GitHub Personal Access Token setup |
| ❌ **More Dependencies** | Larger dependency footprint (Retrofit, OkHttp, interceptors) |
| ❌ **Initial Setup** | Requires token generation and configuration |

### Setup

#### 1. Generate a GitHub Token

1. Go to [GitHub Settings → Tokens](https://github.com/settings/tokens)
2. Click "Generate new token" (classic or fine-grained)
3. Select required scopes:
   - `repo` (for private repositories)
   - `read:org` (for organization data)
   - `read:user` (for user data)
4. Copy the generated token

#### 2. Configure `local.properties`

```properties
# Use Retrofit client (default)
api_client_type=RETROFIT

# GitHub Personal Access Token (required)
access_token=ghp_your_token_here
# Or for fine-grained tokens:
# access_token=github_pat_11AA22BB33CC44DD_your_token_here
```

#### 3. Optional: Enable Database Caching

For persistent caching across runs:

```properties
# PostgreSQL cache configuration
db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
db_cache_username=your_username
db_cache_password=your_password
db_cache_expiration_hours=168
```

### Best For

- ✅ **Production use** with high request volumes
- ✅ **CI/CD environments** requiring consistent performance
- ✅ **Automated systems** needing reliable operation
- ✅ **When HTTP caching** is important for performance
- ✅ **When you need** structured error handling

---

## GitHub CLI

An alternative implementation that uses the official [GitHub CLI](https://cli.github.com/) (`gh` command) for API requests.

### Advantages

| Advantage | Description |
|-----------|-------------|
| ✅ **Simple Setup** | Uses existing `gh` CLI authentication |
| ✅ **No Token Management** | Leverages `gh auth login` credentials |
| ✅ **Official Tool** | Uses GitHub's official CLI |
| ✅ **Fewer Dependencies** | Only needs Moshi for JSON parsing |
| ✅ **Lower Memory Usage** | Process-based execution uses less memory |

### Disadvantages

| Disadvantage | Description |
|--------------|-------------|
| ❌ **No HTTP Caching** | Each request requires shell execution |
| ❌ **Slower Performance** | Shell execution adds ~100-300ms overhead per request |
| ❌ **External Dependency** | Requires `gh` CLI to be installed |
| ❌ **Less Debugging Visibility** | Errors come as CLI text output |
| ❌ **Sequential Execution** | Not efficient for parallel requests |

### Setup

#### 1. Install GitHub CLI

**macOS:**
```bash
brew install gh
```

**Windows:**
```bash
winget install --id GitHub.cli
```

**Linux (Debian/Ubuntu):**
```bash
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh
```

See [cli.github.com](https://cli.github.com/) for more installation options.

#### 2. Authenticate with GitHub

```bash
gh auth login
```

Follow the prompts to authenticate via browser or token.

#### 3. Configure `local.properties`

```properties
# Use GitHub CLI client
api_client_type=GH_CLI

# Optional: Set command timeout (default: 10 seconds)
gh_cli_timeout_seconds=15

# Note: access_token is NOT required for GH_CLI
# Authentication uses `gh auth login`
```

#### 4. Optional: Enable Database Caching

To improve performance with persistent caching:

```properties
# PostgreSQL cache configuration (recommended for GH_CLI)
db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
db_cache_username=your_username
db_cache_password=your_password
db_cache_expiration_hours=168
```

### Best For

- ✅ **Local development** and testing
- ✅ **Quick one-off runs** on your machine
- ✅ **When `gh` CLI** is already configured
- ✅ **Learning and experimentation**
- ✅ **When you prefer** CLI-based authentication

---

## Performance Comparison

### Response Time Benchmarks

| Scenario | Retrofit | GH CLI | Notes |
|----------|----------|--------|-------|
| **Cold start** | ~100-200ms | ~500-1000ms | GH CLI has shell startup overhead |
| **Average request** | ~145ms | ~285ms | Direct HTTP vs. shell execution |
| **Cached request (HTTP)** | ~5-10ms | N/A | Retrofit HTTP cache hits |
| **Cached request (DB)** | ~10-20ms | ~10-20ms | Both use PostgreSQL cache |

### Cache Performance

| Feature | Retrofit | GH CLI |
|---------|----------|--------|
| **HTTP Cache** | ✅ Automatic | ❌ Not available |
| **Database Cache** | ✅ Optional | ✅ Optional |
| **Expected Hit Rate** | ~60-80% | ~50-70% (DB only) |
| **Cache Location** | `http-cache/` + DB | DB only |

### Resource Usage

| Resource | Retrofit | GH CLI |
|----------|----------|--------|
| **Memory** | Higher (connection pool) | Lower (process-based) |
| **CPU** | Lower (efficient HTTP) | Higher (shell spawning) |
| **Disk** | HTTP cache files | None (without DB cache) |

---

## Caching Strategy

### Retrofit Caching

1. **HTTP Cache (Automatic)**
   - Located in `http-cache/` directory
   - Follows GitHub API cache headers
   - Transparent to application code

2. **Database Cache (Optional)**
   - Persistent across application restarts
   - Configurable expiration (default: 168 hours)
   - Shared across multiple runs

### GH CLI Caching

1. **Database Cache (Recommended)**
   - Compensates for lack of HTTP caching
   - Same configuration as Retrofit
   - See [GH CLI Caching Guide](GH_CLI_CACHING.md) for details

### Cache Expiration Guidelines

| Data Type | Recommended TTL | Reason |
|-----------|-----------------|--------|
| Old/closed PRs | 168 hours (7 days) | Data rarely changes |
| Recent/open PRs | 24 hours | Active development |
| High-frequency analysis | 4-8 hours | Fresh data needed |

---

## Migration Guide

### Switching from Retrofit to GH CLI

1. **Install GitHub CLI:**
   ```bash
   brew install gh  # macOS
   gh auth login
   ```

2. **Update `local.properties`:**
   ```properties
   # Change from RETROFIT to GH_CLI
   api_client_type=GH_CLI
   
   # Token no longer required (can be removed)
   # access_token=ghp_xxx
   ```

3. **Consider enabling database cache** for better performance:
   ```properties
   db_cache_url=jdbc:postgresql://localhost:5432/github_stats_cache
   db_cache_username=your_username
   db_cache_password=your_password
   ```

4. **Run the application** - no code changes needed!

### Switching from GH CLI to Retrofit

1. **Generate a GitHub token** at [github.com/settings/tokens](https://github.com/settings/tokens)

2. **Update `local.properties`:**
   ```properties
   # Change from GH_CLI to RETROFIT
   api_client_type=RETROFIT
   
   # Add your token
   access_token=ghp_your_token_here
   ```

3. **Run the application** - HTTP caching will automatically improve performance

### Data Compatibility

- ✅ Both clients return **identical data structures**
- ✅ Database cache is **shared** between implementations
- ✅ No data migration needed when switching
- ✅ Reports generated by either client are **identical**

---

## Rate Limiting

Both implementations are subject to GitHub's API rate limits:

| Rate Limit | Authenticated | Unauthenticated |
|------------|---------------|-----------------|
| **Requests/hour** | 5,000 | 60 |
| **Search requests/minute** | 30 | 10 |

### Rate Limit Strategies

| Strategy | Retrofit | GH CLI |
|----------|----------|--------|
| **HTTP caching** | ✅ Reduces API calls | ❌ Not available |
| **Database caching** | ✅ Optional | ✅ Optional |
| **Request delays** | ✅ Built-in | ✅ Built-in |
| **Rate limit headers** | ✅ Available | ⚠️ Requires parsing |

### Recommendations

- **Enable database caching** when running repeated analyses
- **Use Retrofit** for high-volume operations (better cache hit rate)
- **Check rate limits** before running large date ranges
- **Use delays** between requests (built into the application)

---

## Troubleshooting

### Retrofit Issues

| Problem | Solution |
|---------|----------|
| `HTTP 401 Unauthorized` | Verify `access_token` in `local.properties` is valid |
| `HTTP 403 Rate limit exceeded` | Wait for reset or enable caching |
| `HTTP 404 Not Found` | Check repository owner/name and token permissions |
| Network errors | Check internet connection and firewall settings |

### GH CLI Issues

| Problem | Solution |
|---------|----------|
| `gh: command not found` | Install GitHub CLI: `brew install gh` |
| `gh: Not logged in` | Run `gh auth login` to authenticate |
| Slow performance | Enable database caching |
| Command timeout | Increase `gh_cli_timeout_seconds` in config |

### Common Issues

| Problem | Solution |
|---------|----------|
| No cache hits | Run the same analysis twice to populate cache |
| Stale data | Reduce `db_cache_expiration_hours` or clear cache |
| Missing PR data | Ensure token has access to the repository |

---

## FAQ

### Which client should I use?

**Use Retrofit (default) if:**
- Running in production or CI/CD
- Processing many repositories or large date ranges
- Performance is important
- You want automatic HTTP caching

**Use GH CLI if:**
- Doing local development/testing
- `gh` CLI is already set up on your machine
- You prefer not managing tokens manually
- Running quick one-off analyses

### Can I switch between implementations?

Yes! Both implementations use the same interface and return identical data. Simply change `api_client_type` in `local.properties` and restart the application. No data migration is needed.

### How do I know which client is being used?

Check the application startup logs:
- Retrofit: `"RetrofitApiClient initialized..."`
- GH CLI: `"GhCliApiClient initialized - using GitHub CLI..."`

### Why is GH CLI slower?

GH CLI executes a shell process for each API request, adding overhead:
- Process spawning: ~50-100ms
- Shell initialization: ~50-100ms
- Command execution: similar to Retrofit

### Do both clients respect rate limits?

Yes, both clients:
- Use the same underlying GitHub API
- Are subject to identical rate limits
- Include built-in delays between requests
- Benefit from caching to reduce API calls

### Can I use both clients together?

The application uses one client at a time (configured in `local.properties`). However, both can share the same database cache if configured.

### What permissions does my token need?

For Retrofit client, your token needs:
- `repo` - Access repository data
- `read:org` - Read organization data (if analyzing org repos)
- `read:user` - Read user profile data

GH CLI uses its own authentication scope from `gh auth login`.

---

## Related Documentation

- [API Client Compatibility Matrix](API_CLIENT_COMPATIBILITY.md) - Detailed technical comparison
- [GH CLI Caching Guide](GH_CLI_CACHING.md) - Database caching for GH CLI
- [GH CLI Logging Guide](GH_CLI_LOGGING.md) - Debug logging configuration
- [Setup Guide](../SETUP.md) - Project setup instructions
- [README](../README.md) - Project overview

## External Resources

- [GitHub REST API Documentation](https://docs.github.com/en/rest)
- [GitHub CLI Documentation](https://cli.github.com/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [OkHttp Documentation](https://square.github.io/okhttp/)
