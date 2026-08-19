[![Gradle CI](https://github.com/hossain-khan/github-stats/actions/workflows/build.yml/badge.svg)](https://github.com/hossain-khan/github-stats/actions/workflows/build.yml) [![codecov](https://codecov.io/gh/hossain-khan/github-stats/graph/badge.svg?token=447IZ4HE0B)](https://codecov.io/gh/hossain-khan/github-stats)

# GitHub Stats 📈
Playground for exploring Github API and collect some PR review stats for different users/contributors.

> 🚧 Initial work done (see limitations) 🚧  
> 👷‍♂️ Ready for experimental use!

### Background
This project exist to explore GitHub API to get some common answer about PR review time by different reviewers.

#### What it is NOT
- ❌ It is not a comprehensive GitHub statistics generator tool. Built for specific purpose.
- ❌ It is not a modular tool that can be re-used to query GitHub APIs.
- ❌ Existing supported stats are not localized for different work hour or weekends. Supports North American timezones (and limited other cities).
- ❌ It does not follow all industry standards, and does not strive to be performant either.


#### What it is ✔️
- ✅ It can show you PR statistics for PRs created by specific author/contributor of a repository
- ✅ It can generate CSV for the PR stats which can be used in Google Sheets or alike to generate charts
- ✅ It can also generate basic chart/graph using Google Chart to visualize the PR stats
##### Limitation
- 🏋️ It does not collect stats in parallel to avoid GitHub API rate-limit and adds delay between API calls, resulting in longer wait time for larger date span with lot of PRs.
- 🏋️ It is **NOT** able to accurately compute PR review time due to many complexities. The review time is provided for informational purpose only.
- 🔐 The generated API token must have access to repository and user in the orginization, otherwise all API request will fail.

## Setup 🛠
See [SETUP](SETUP.md) for details on how to setup the project using IntelliJ IDEA.

### API Client Options
This project supports two API client implementations:
- **Retrofit/OkHttp** (default) - Faster with built-in HTTP caching, requires GitHub token
- **GitHub CLI** - Uses `gh` command, simpler setup with existing CLI authentication

See [API Clients Guide](docs/API_CLIENTS.md) for detailed comparison, setup instructions, and best practices.

### Quick Start
```bash
# 1. Clone the repository
git clone https://github.com/hossain-khan/github-stats.git
cd github-stats

# 2. Initialize local.properties with automatic date setup
./local-prop-init.sh

# 3. Edit local.properties and add your:
#    - GitHub access token
#    - Repository details
#    - Author list

# 4. Run the stats generator
./gradlew run
```

### Run App 📊
After configuration comlete, you can run the app in either ways: 

1. Run the app from IntelliJ ▶️ [Main.kt](https://github.com/hossain-khan/github-stats/blob/main/src/main/kotlin/Main.kt)
2. Run the app from terminal using **`./gradlew run`** command

## How
The program collects all the related data to user's via different GitHub APIs and compiles releavant data into stats. Those stats are then run through [StatsFormatter](https://github.com/hossain-khan/github-stats/blob/main/src/main/kotlin/dev/hossain/githubstats/formatter/StatsFormatter.kt) to generate files.  

Here is an quick overview of how stats generation works.  
<img alt="Stats Generator Flow" width="600" src="https://user-images.githubusercontent.com/99822/200206579-bba022ea-ebe4-4d5c-9a81-b2c4b6ed6090.jpg"/>


## Sample
Here is some data generated from `freeCodeCamp` repository for `naomi-lgbt` user. See 📊 [demo](https://hossain-khan.github.io/github-stats/demo-report/)

<img width="497" alt="Generate files" src="https://github.com/user-attachments/assets/1accc59c-0185-4b3a-9eec-712ae050ea62" />

| Dashboard - Light | Dashboard - Dark | 
| -------- | ---------- |
| <img width="3840" height="12246" alt="Screenshot 2026-08-19 at 15-46-13 freeCodeCamp Analytics Dashboard" src="https://github.com/user-attachments/assets/97116f5d-7006-4246-bc71-755fd8435a85" /> | <img width="3840" height="12246" alt="Screenshot 2026-08-19 at 15-47-06 freeCodeCamp Analytics Dashboard" src="https://github.com/user-attachments/assets/27c16bf3-1c2f-4ea1-b571-506c83c53949" /> |
| - | - |
| **PR Author - Light** | **PR Author - Dark** | 
| <img width="3840" height="3864" alt="Screenshot 2026-08-19 at 15-46-40 Author Analytics - majestic-owl448 (freeCodeCamp)" src="https://github.com/user-attachments/assets/2ed04801-a535-4d0f-9d59-2eece8fb6795" /> | <img width="3840" height="3864" alt="Screenshot 2026-08-19 at 15-46-50 Author Analytics - majestic-owl448 (freeCodeCamp)" src="https://github.com/user-attachments/assets/4ab6b16b-61c2-498c-b69c-8382f0a534b3" /> |
| - | - |
| **Reviewer - Light** | **Reviewer - Dark** | 
| <img width="3840" height="3192" alt="Screenshot 2026-08-19 at 15-49-23 Reviewer Analytics - DanielRosa74 (freeCodeCamp)" src="https://github.com/user-attachments/assets/7b136d9e-812b-4a47-9703-429010339cad" /> | <img width="3840" height="3192" alt="Screenshot 2026-08-19 at 15-49-16 Reviewer Analytics - DanielRosa74 (freeCodeCamp)" src="https://github.com/user-attachments/assets/bde02d24-d109-433b-8cd4-7bbfe19b5608" /> |



### Agregated Dashboard


### Stats as PR Author


### Stats as PR Reviewer


## References
* https://docs.github.com/en/rest
* https://docs.github.com/en/rest/overview/endpoints-available-for-github-apps
