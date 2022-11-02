package dev.hossain.githubstats.model

/**
 * A GitHub user with basic information needed for stats generation.
 * See [User API](https://docs.github.com/en/rest/users/users#get-a-user) for more info.
 *
 * Example JSON
 * ```json
 * {
 *   "login": "swankjesse",
 *   "id": 133019,
 *   "node_id": "MDQ6VXNlcjEzMzAxOQ==",
 *   "avatar_url": "https://avatars.githubusercontent.com/u/133019?v=4",
 *   "gravatar_id": "",
 *   "url": "https://api.github.com/users/swankjesse",
 *   "html_url": "https://github.com/swankjesse",
 *   "followers_url": "https://api.github.com/users/swankjesse/followers",
 *   "following_url": "https://api.github.com/users/swankjesse/following{/other_user}",
 *   "gists_url": "https://api.github.com/users/swankjesse/gists{/gist_id}",
 *   "starred_url": "https://api.github.com/users/swankjesse/starred{/owner}{/repo}",
 *   "subscriptions_url": "https://api.github.com/users/swankjesse/subscriptions",
 *   "organizations_url": "https://api.github.com/users/swankjesse/orgs",
 *   "repos_url": "https://api.github.com/users/swankjesse/repos",
 *   "events_url": "https://api.github.com/users/swankjesse/events{/privacy}",
 *   "received_events_url": "https://api.github.com/users/swankjesse/received_events",
 *   "type": "User",
 *   "site_admin": false
 * }
 * ```
 */
data class User(
    val login: String,
    val type: String?,
    val url: String?,
    val html_url: String?,
    val avatar_url: String?,
    val id: Long,
    val repos_url: String?
)
