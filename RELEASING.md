# Release Guide

This document outlines the step-by-step release process for **GitHub Stats**.

---

## 📋 Prerequisites

- **Git & GitHub CLI (`gh`)**: Authenticated with release permissions (`gh auth login`).
- **JDK 21**: Configured locally (Temurin recommended).
- **Clean Working Tree**: Ensure all feature branches are merged and CI is passing on `main`.

---

## 🚀 Release Process

### Step 1: Create a Release Branch
Create a new branch for the release from `main`:
```bash
git checkout main
git pull origin main
git checkout -b release/vX.Y
```
*(Replace `X.Y` with the target release version, e.g. `1.10`)*

---

### Step 2: Bump Application Version
Update the `version` property in [`build.gradle.kts`](build.gradle.kts):
```kotlin
group = "dev.hossain.githubstats"
version = "X.Y" // e.g. "1.10"
```

---

### Step 3: Run Pre-Release Verification
Run the full test suite, linter, and coverage reports locally:
```bash
# Format and lint check
./gradlew formatKotlin
IS_GITHUB_CI=true ./gradlew lintKotlin test koverHtmlReport
```
Ensure all tests pass and there are zero lint violations.

---

### Step 4: Submit & Merge Release PR
Commit the version bump and open a pull request:
```bash
git add build.gradle.kts
git commit -m "Bump version to X.Y for release"
git push -u origin release/vX.Y
gh pr create --title "Release vX.Y" --body "Bump version to X.Y for release."
```
Once CI passes, review and merge the PR into `main`.

---

### Step 5: Create & Publish GitHub Release
Once merged to `main`, pull the latest commit and publish the release with generated release notes:

```bash
git checkout main
git pull origin main

# Create and publish the GitHub release with git tag
gh release create vX.Y --title "Release vX.Y" --generate-notes
```

Alternatively, publish via the GitHub Web UI:
1. Navigate to [GitHub Releases](https://github.com/hossain-khan/github-stats/releases).
2. Click **Draft a new release**.
3. Choose or create tag `vX.Y`.
4. Click **Generate release notes** and curate the highlights.
5. Click **Publish release**.

---

### Step 6: Post-Release Verification

- **GitHub Pages / Dokka API Docs**: The [static-docs.yml](.github/workflows/static-docs.yml) workflow will automatically build and publish updated Dokka documentation to [GitHub Pages](https://hossain-khan.github.io/github-stats/).
- **GitHub Actions CI**: Verify that all automated workflows pass on the `main` branch.
