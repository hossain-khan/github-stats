# GitHub Stats - Copilot Agent Instructions

## Repository Overview

**Purpose**: A Kotlin-based CLI application that explores GitHub API to collect PR review statistics for different users/contributors. It generates reports (CSV, HTML, ASCII) showing PR review times, reviewer stats, and PR author statistics.

**Key Features**:
- Collects PR statistics for specified authors/reviewers from GitHub repositories
- Generates multiple report formats: CSV, HTML (with Google Charts), ASCII tables, and aggregated dashboards
- Supports two API clients: Retrofit (default, faster with caching) and GitHub CLI
- Uses OkHttp caching and optional PostgreSQL database caching to minimize API calls
- Designed for North America timezone, not localized for different work hours/weekends

## Project Configuration

**Language**: Kotlin 2.2.21  
**Build Tool**: Gradle 9.2.1 with Kotlin DSL  
**JDK Version**: Java 17 (with JVM target 17)  
**Runtime**: JVM application  
**Code Size**: ~7,900 lines of Kotlin code  

### Key Dependencies
- **Networking**: OkHttp 5.3.2, Retrofit 3.0.0, Moshi 1.15.2
- **Coroutines**: kotlinx-coroutines-core 1.10.2
- **Database**: SQLDelight 2.2.1 with PostgreSQL driver
- **DI Framework**: Koin 4.1.1
- **Testing**: JUnit 5, MockK 1.14.6, Truth, mockwebserver
- **Reporting**: Picnic 0.7.0 (ASCII tables), kotlin-csv 1.10.0

## Build & Test Commands

### ⚠️ CRITICAL: Environment Setup for Tests
**ALWAYS set `IS_GITHUB_CI=true` environment variable when running tests or build in CI/automation contexts.** Without this, tests will fail looking for `local.properties`.

### Commands (in order of typical workflow)

1. **Lint Kotlin code** (ALWAYS run before committing):
   ```bash
   ./gradlew lintKotlin
   ```
   - Takes ~15-20 seconds
   - Uses kotlinter plugin with ktlint rules
   - Checks both main and test sources
   - Must pass before build

2. **Format Kotlin code** (if lint fails):
   ```bash
   ./gradlew formatKotlin
   ```
   - Auto-fixes formatting issues

3. **Build project** (compiles + runs tests):
   ```bash
   IS_GITHUB_CI=true ./gradlew build
   ```
   - Takes ~45-60 seconds
   - Includes: SQLDelight code generation, Kotlin compilation, test execution
   - **MUST use `IS_GITHUB_CI=true`** or tests will fail with "Please create local.properties" error
   - SQLDelight generates database interface from `.sq` files during build

4. **Run tests only**:
   ```bash
   IS_GITHUB_CI=true ./gradlew test
   ```
   - Takes ~5-10 seconds (if already compiled)
   - Test report: `build/reports/tests/test/index.html`

5. **Run application**:
   ```bash
   ./gradlew run
   ```
   - Requires `local.properties` configuration (or `IS_GITHUB_CI=true` to use sample config)
   - Will fail with DNS/network error in restricted environments
   - Application entry point: `src/main/kotlin/Main.kt`

6. **Clean build artifacts**:
   ```bash
   ./gradlew clean
   ```

7. **Generate API documentation** (Dokka):
   ```bash
   ./gradlew dokkaGeneratePublicationHtml
   ```
   - Output: `build/dokka/html/`

### Common Build Issues & Workarounds

**Issue**: Tests fail with `IllegalStateException: Please create local.properties`  
**Solution**: Set environment variable `IS_GITHUB_CI=true` before running gradle commands

**Issue**: SQLDelight compilation errors  
**Solution**: Run `./gradlew clean` then rebuild. SQLDelight generates code during build from `src/main/sqldelight/**/*.sq` files.

**Issue**: Gradle daemon issues  
**Solution**: Use `./gradlew --stop` to stop daemon, then retry build

## Project Structure & Architecture

### Root Directory Files
```
build.gradle.kts          # Main build configuration (dependencies, plugins, tasks)
settings.gradle.kts       # Project settings, defines root project name
gradle.properties         # Gradle properties
local_sample.properties   # Sample configuration template
local-prop-init.sh        # Script to initialize local.properties with dates
README.md                 # Comprehensive project documentation
SETUP.md                  # Setup walkthrough with screenshots
LICENSE                   # Project license
renovate.json             # Renovate bot configuration
.gitignore                # Git ignore rules (includes local.properties, REPORTS-*, http-cache/*)
```

### Source Code Layout

**Main application code**: `src/main/kotlin/`
- `Main.kt` - Application entry point, initializes Koin DI and runs stats generation
- `StatsGeneratorApplication.kt` - Main stats generation logic coordinator
- `PrStatsApplication.kt` - PR stats generation for single user
- `CodeSnippets.kt` - Example usage snippets

**Package structure**:
- `dev.hossain.githubstats/`
  - `client/` - API clients (RetrofitApiClient, GhCliApiClient)
  - `repository/` - Data repositories (PullRequestStatsRepoImpl)
  - `service/` - Pagination services (IssueSearchPagerService, TimelineEventsPagerService)
  - `model/` - Data models (PullRequest, Timeline events, etc.)
  - `formatter/` - Report generators (CSV, HTML, ASCII, Dashboard)
  - `cache/` - Caching implementation (OkHttp, PostgreSQL database cache)
  - `io/` - GitHub API service interface definitions (Client.kt)
  - `util/` - Utilities (PropertiesReader, ErrorProcessor, FileUtil)
  - `di/` - Dependency injection modules
  - `logging/` - Logging utilities
- `dev.hossain.time/` - Time-related utilities and extensions
- `dev.hossain.ascii/` - ASCII art for CLI output
- `dev.hossain.i18n/` - Internationalization strings

**Database**: `src/main/sqldelight/dev/hossain/githubstats/cache/database/`
- `ResponseCache.sq` - SQLDelight schema for PostgreSQL caching

**Resources**: `src/main/resources/`
- `strings.properties` - Application strings

**Tests**: `src/test/kotlin/` - Mirrors main source structure

### Configuration Files
- **Build**: `build.gradle.kts`, `settings.gradle.kts`
- **Linting**: kotlinter plugin configured in `build.gradle.kts` (lines 9, 126-131)
- **Application Config**: `local.properties` (not in repo, copy from `local_sample.properties`)
- **Gradle Wrapper**: `gradle/wrapper/gradle-wrapper.properties` (Gradle 9.2.1)

## GitHub Actions CI/CD

### Workflow: `.github/workflows/build.yml`
**Trigger**: Push/PR to `main` branch  
**Environment**: Ubuntu latest, JDK 21 (Temurin distribution)  
**Steps**:
1. Cache Gradle files (`~/.gradle/caches`, `~/.gradle/wrapper`)
2. Checkout code
3. Setup JDK 21 (note: build.gradle uses JDK 17, CI uses 21 - compatible)
4. Grant execute permission: `chmod +x gradlew`
5. **Kotlin Lint**: `./gradlew lintKotlin` (MUST pass)
6. **Build**: `./gradlew build` (includes tests)
7. Success confirmation

**Important**: `IS_GITHUB_CI=true` environment variable set at workflow level (line 10)

### Workflow: `.github/workflows/static-docs.yml`
**Trigger**: Push to `main`, manual dispatch  
**Purpose**: Generate and deploy Dokka documentation to GitHub Pages  
**Steps**: Build → Generate Dokka HTML → Deploy to Pages

## Local Development Setup

### Required Configuration
1. **Initialize config** (recommended):
   ```bash
   ./local-prop-init.sh
   ```
   - Creates `local.properties` from `local_sample.properties`
   - Auto-sets `date_limit_after` to 1 month ago
   - Auto-sets `date_limit_before` to today

2. **Manual configuration**: Copy `local_sample.properties` to `local.properties`

3. **Required values in local.properties**:
   - `access_token` - GitHub personal access token (classic or fine-grained)
   - `repository_owner` - GitHub org/username
   - `repository_id` - Repository name
   - `authors` - Comma-separated list of GitHub usernames
   - `date_limit_after` - Start date (YYYY-MM-DD format)
   - Optional: `date_limit_before`, `bot_users`, database cache settings

### Running the Application
- **IDE**: Run `Main.kt` directly in IntelliJ IDEA
- **CLI**: `./gradlew run`
- **Output**: Reports generated in project root as `REPORTS-*` files (gitignored)

## Key Implementation Details

### API Rate Limiting Strategy
- Adds delays between API calls to avoid GitHub rate limits
- Sequential processing (not parallel) to control API usage
- Uses HTTP caching (`http-cache/` directory) to reduce duplicate requests
- Optional PostgreSQL database caching for persistent cache across runs

### Testing Strategy
- Unit tests use MockWebServer for API mocking
- Tests require `IS_GITHUB_CI=true` environment variable or `local.properties` file
- PropertiesReader checks for `IS_GITHUB_CI` env var and uses `local_sample.properties` if true
- Most tests in `src/test/kotlin/dev/hossain/githubstats/`

### Code Quality Checks
1. **Linting**: kotlinter (ktlint) - enforced in CI, must pass before build
2. **Build**: Kotlin compilation with strict warnings
3. **Tests**: JUnit 5 with comprehensive unit tests (244 tests total)

## Important Development Guidelines

### When Making Changes:

1. **ALWAYS lint before committing**:
   ```bash
   ./gradlew lintKotlin
   ```
   If it fails, run `./gradlew formatKotlin` to auto-fix

2. **ALWAYS test with `IS_GITHUB_CI=true`**:
   ```bash
   IS_GITHUB_CI=true ./gradlew build
   ```

3. **SQLDelight changes**: If modifying `.sq` files, clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

4. **Generated files**: Exclude from manual edits:
   - `build/generated/sqldelight/` - SQLDelight generated code
   - `http-cache/*` - HTTP cache files
   - `REPORTS-*` - Generated reports

5. **Configuration**: Never commit `local.properties` (in .gitignore)

### Code Style
- Kotlin idiomatic style enforced by kotlinter
- Maximum line length: Check kotlinter rules
- Suppress specific rules with `@Suppress("ktlint:standard:rule-name")`

### Dependencies
- Update via `build.gradle.kts` dependencies block
- Renovate bot manages dependency updates (see `renovate.json`)
- After adding dependencies, run full build to ensure compatibility

## Validation Pipeline

The repository enforces the following checks before merging:

1. **Required Check**: "Success" step in build.yml workflow
2. **Linting**: Must pass `./gradlew lintKotlin`
3. **Build**: Must pass `./gradlew build` (includes compilation + tests)
4. **Environment**: Tests run with `IS_GITHUB_CI=true`

### To Replicate CI Locally:
```bash
# Exactly what CI does:
chmod +x gradlew
./gradlew lintKotlin
IS_GITHUB_CI=true ./gradlew build
```

## Additional Notes

- **Timezone**: Stats calculations assume North America timezone, not localized
- **Performance**: Not optimized for speed; adds delays to respect API rate limits
- **Review Time Accuracy**: PR review time calculations are approximate, not precise
- **Bot Users**: Can be filtered out via `bot_users` config (e.g., renovate[bot], dependabot)
- **API Client Types**: Supports RETROFIT (default) or GH_CLI (requires `gh` CLI installed)

### Documentation Files
- `README.md` - Usage, features, examples with screenshots
- `SETUP.md` - Step-by-step setup guide with screenshots
- `docs/GH_CLI_CACHING.md` - GitHub CLI caching documentation
- `docs/GH_CLI_LOGGING.md` - GitHub CLI logging documentation

## Trust These Instructions

These instructions have been validated by running all commands and examining the actual codebase. Only search for additional information if:
- These instructions are incomplete for your specific task
- You encounter errors not documented here
- You need to understand implementation details not covered above

When in doubt, refer to `README.md` and `SETUP.md` for additional context.
