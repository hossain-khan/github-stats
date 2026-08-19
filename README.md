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
Here is some data generated from `freeCodeCamp` repository for `naomi-lgbt` user.

<img width="497" alt="Generate files" src="https://github.com/user-attachments/assets/1accc59c-0185-4b3a-9eec-712ae050ea62" />

### Agregated Dashboard

The application generates an aggregated HTML dashboard that combines different reports into a single page. See `REPORTS-[reponame]-AGGREGATED/REPORT_-_aggregated-pr-stats-for-all-authors.html`

<img width="3712" height="6706" alt="Image" src="https://github.com/user-attachments/assets/c31a532c-d4cc-4084-96c3-d1869b815308" />

### Stats as PR Author
Here is example chart generated from the report as HTML output.

<img width="1919" alt="Image" src="https://github.com/user-attachments/assets/39565e04-36c6-4019-873d-6b34601ab737" />
<img width="1165" alt="Image" src="https://github.com/user-attachments/assets/b72b952b-8e17-4aec-a454-628b57ac9c89" />


<details>
<summary>📊 Stats Report</summary>
  
```
  -------------------------------------------------------------------------------------------------------------------  
  PR reviewer's stats for PRs created by 'naomi-lgbt' on 'freeCodeCamp' repository between 2024-07-01 and 2024-12-31.  
  -------------------------------------------------------------------------------------------------------------------  
                                                                                                                       
┌─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                                                     │
│ ● PR reviewer stats for "moT01"                                                                                     │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 3                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 1d 14h 34m for PR#57153                            │
│                                                                │ made 1 code review comment and 0 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 13h for PR#56463                                   │
│                                                                │ made 1 code review comment and 1 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 2h 45m 1s for PR#55638                             │
│                                                                │ made 0 code review comment and 1 issue comment.    │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 18h 6m 20.333333333s                               │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "huyenltnguyen"                                                                             │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 3                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 20m 48s for PR#56772                               │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 16h 48m for PR#56417                               │
│                                                                │ made 5 code review comments and 0 issue comment.   │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 3h for PR#55549                                    │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 6h 42m 56s                                         │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "gikf"                                                                                      │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 3                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 1h for PR#56376                                    │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 12h for PR#55665                                   │
│                                                                │ made 1 code review comment and 0 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 3h for PR#55549                                    │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 5h 20m                                             │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "ojeytonwilliams"                                                                           │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 3                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 1h for PR#56376                                    │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 12m 28s for PR#55638                               │
│                                                                │ made 1 code review comment and 0 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 11h 40m for PR#55403                               │
│                                                                │ made 1 code review comment and 0 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 4h 17m 29.333333333s                               │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "ilenia-magoni"                                                                             │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 2                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 14m 36s for PR#56772                               │
│                                                                │ made 1 code review comment and 0 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 5m 54s for PR#56463                                │
│                                                                │ made 4 code review comments and 0 issue comment.   │
│                                                                │ also has reviewed PR 4 times.                      │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 10m 15s                                            │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "a2937"                                                                                     │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 2                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 26m 51s for PR#56772                               │
│                                                                ├────────────────────────────────────────────────────┤
│                                                                │ 9h 4m for PR#56417                                 │
│                                                                │ made 0 code review comment and 1 issue comment.    │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 4h 45m 25.5s                                       │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "jdwilkin4"                                                                                 │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 1                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 5h 45m for PR#57153                                │
│                                                                │ made 2 code review comments and 0 issue comment.   │
│                                                                │ also has reviewed PR 1 time.                       │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 5h 45m                                             │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "lasjorg"                                                                                   │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 1                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 17h 14m for PR#55665                               │
│                                                                │ made 1 code review comment and 1 issue comment.    │
│                                                                │ also has reviewed PR 1 time.                       │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 17h 14m                                            │
├────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┤
│                                                                                                                     │
│ ● PR reviewer stats for "raisedadead"                                                                               │
├────────────────────────────────────────────────────────────────┬────────────────────────────────────────────────────┤
│ Total Reviews                                                  │ 1                                                  │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Review Durations                                               │ 12h for PR#55403                                   │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│ Average Time                                                   │ 12h                                                │
├────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────┤
│                                                                │                                                    │
│                                                                │                                                    │
│ ● Average PR Merge Time for all PRs created by 'naomi-lgbt'    │ 1d 20h 37m 19.578947368s                           │
└────────────────────────────────────────────────────────────────┴────────────────────────────────────────────────────┘
```
</details>  
  

### Stats as PR Reviewer
And this chart is user as PR reviewer, reviewing other user's PRs.

<img width="1919" alt="Image" src="https://github.com/user-attachments/assets/838f94e4-b502-4347-83ac-ff5faa47412f" />
<img width="779" alt="Image" src="https://github.com/user-attachments/assets/2ef4892f-d55c-4da9-83be-68aebaa6d803" />

<details>
<summary>📊 Stats Report</summary>
  
```
  --------------------------------------------------------------------------------------------------------------  
  Stats for all PR reviews given by 'naomi-lgbt' on 'freeCodeCamp' repository between 2024-07-01 and 2024-12-31.  
  --------------------------------------------------------------------------------------------------------------  
                                                                                                                  
┌───────────────────────────────────────────────┬────────────────────────────────────────────────────────────────┐
│ Total Reviews                                 │ 301                                                            │
├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ Average Review Time                           │ 20h 36m 10.611295681s                                          │
├───────────────────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ PR Authors Reviewed For                       │ ✔ 1 PR reviewed for 'JungLee-Dev'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'anandhelloworld'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'YashJsh'                                  │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'timmy471'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'altsun'                                   │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Abhishek-dev479'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'itzVarsha'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'X1Vi'                                     │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'charlotte-whiting'                        │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'zairahira'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'AbhilashK26'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'bytexenon'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'kamalxdev'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Abhi0049k'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Bharatkgupta'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'SahilWMI'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Joes131205'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'AryanBhirud'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Muhammad-Rebaal'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'uditbaliyan'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'suryaanshah'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Arnavthakare19'                           │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Chioma-Okeke'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'IAMOTZ'                                   │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'jeremylt'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'aakarsh-2004'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Othniel01'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'SelormDev'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'khatri7'                                  │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'rachhanari'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'BethiPooja'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'doosabhulaxmi'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'JoT8ng'                                   │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'clydehenry3'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'EggSaled'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'nikkivaddepelli'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'cristinajeandonato'                       │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Sebastian-Wlo'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'franfreezy'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'OlibhiaGhosh'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'rakshixh'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'bulutyerli'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'hedocode'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Dhairya-A-Mehra'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'pragyananda'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'CBID2'                                    │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Nycto-c05'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'shubha987'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'AnarchistHoneybun'                        │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'mendoncamaria'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'AaryanProthi'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'spartanns'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Code-Hacker26'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'devshah207'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'dareckolo'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'CracktheDom'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'Ayushjhawar8'                             │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'nayanmapara'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'farihaNaqvi'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'dyadyaJora'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'uzma-nazim'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'scissorsneedfoodtoo'                      │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'dilankayasuru'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'kevin-wu01'                               │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'raisedadead'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 1 PR reviewed for 'dwrik'                                    │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 2 PRs reviewed for 'Dario-DC'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 2 PRs reviewed for 'Ksound22'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 2 PRs reviewed for 'miyaliu666'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 2 PRs reviewed for 'royjohnlee'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 2 PRs reviewed for 'hittrow'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 3 PRs reviewed for 'Sembauke'                                │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 3 PRs reviewed for 'Ritesh2351235'                           │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 4 PRs reviewed for 'ahmaxed'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 4 PRs reviewed for 'gikf'                                    │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 5 PRs reviewed for 'ilenia-magoni'                           │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 5 PRs reviewed for 'gagan-bhullar-tech'                      │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 6 PRs reviewed for 'DanielRosa74'                            │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 8 PRs reviewed for 'huyenltnguyen'                           │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 8 PRs reviewed for 'ShaunSHamilton'                          │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 9 PRs reviewed for 'lasjorg'                                 │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 19 PRs reviewed for 'moT01'                                  │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 20 PRs reviewed for 'a2937'                                  │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 34 PRs reviewed for 'jdwilkin4'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 40 PRs reviewed for 'camperbot'                              │
│                                               ├────────────────────────────────────────────────────────────────┤
│                                               │ ✔ 57 PRs reviewed for 'ojeytonwilliams'                        │
└───────────────────────────────────────────────┴────────────────────────────────────────────────────────────────┘
```
</details>

## References
* https://docs.github.com/en/rest
* https://docs.github.com/en/rest/overview/endpoints-available-for-github-apps
