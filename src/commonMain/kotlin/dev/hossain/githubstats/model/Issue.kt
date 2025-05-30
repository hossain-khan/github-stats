package dev.hossain.githubstats.model

/**
 * GitHub issue - that can be regular issue or pull-request.
 *
 * Example JSON:
 * ```
 * {
 *   "url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/46973",
 *   "repository_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp",
 *   "labels_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/46973/labels{/name}",
 *   "comments_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/46973/comments",
 *   "events_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/46973/events",
 *   "html_url": "https://github.com/freeCodeCamp/freeCodeCamp/pull/46973",
 *   "id": 1311900370,
 *   "node_id": "PR_kwDOAbI7X847x6vT",
 *   "number": 46973,
 *   "title": "Adding meta-tags in Portuguese",
 *   "user": {
 *     "login": "DanielRosa74"
 *   },
 *   "labels": [],
 *   "state": "closed",
 *   "locked": false,
 *   "assignee": null,
 *   "assignees": [],
 *   "milestone": null,
 *   "comments": 2,
 *   "created_at": "2022-07-20T20:53:04Z",
 *   "updated_at": "2022-07-21T17:18:11Z",
 *   "closed_at": "2022-07-21T17:18:10Z",
 *   "author_association": "CONTRIBUTOR",
 *   "active_lock_reason": null,
 *   "draft": false,
 *   "pull_request": {
 *     "merged_at": "2022-07-21T17:18:10Z"
 *   },
 *   "body": "Checklist: review PR",
 *   "reactions": {},
 *   "timeline_url": "https://api.github.com/repos/freeCodeCamp/freeCodeCamp/issues/46973/timeline",
 *   "performed_via_github_app": null,
 *   "state_reason": null,
 *   "score": 1.0
 * }
 * ```
 */
data class Issue(
    val id: Long,
    /**
     * Issue or PR number.
     * Number uniquely identifying the pull request within its repository.
     */
    val number: Int,
    val state: String,
    val title: String,
    val url: String,
    val html_url: String,
    val user: User,
    val merged: Boolean?,
    val created_at: String,
    val updated_at: String?,
    val closed_at: String?,
    val pull_request: IssuePullRequest?,
)
