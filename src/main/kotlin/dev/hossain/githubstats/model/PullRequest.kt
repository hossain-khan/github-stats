package dev.hossain.githubstats.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

/**
 * A GitHub PR (Pull Request).
 * Pull requests let you tell others about changes you've pushed to a repository on GitHub.
 * Once a pull request is sent, interested parties can review the set of changes,
 * discuss potential modifications, and even push follow-up commits if necessary.
 *
 * Sample JSON (deleted some inner object properties):
 * ```json
 * {
 *   "url": "https://api.github.com/repos/square/retrofit/pulls/3114",
 *   "id": 285207396,
 *   "node_id": "MDExOlB1bGxSZXF1ZXN0Mjg1MjA3Mzk2",
 *   "html_url": "https://github.com/square/retrofit/pull/3114",
 *   "diff_url": "https://github.com/square/retrofit/pull/3114.diff",
 *   "patch_url": "https://github.com/square/retrofit/pull/3114.patch",
 *   "issue_url": "https://api.github.com/repos/square/retrofit/issues/3114",
 *   "number": 3114,
 *   "state": "closed",
 *   "locked": false,
 *   "title": "Add JAXB Converter in Document",
 *   "user": {
 *     "login": "joelhandwell",
 *   },
 *   "body": "By reading documentation, it gives impression that retrofit do not support JAXB without mentioning.",
 *   "created_at": "2019-06-05T02:33:23Z",
 *   "updated_at": "2019-06-06T11:34:48Z",
 *   "closed_at": "2019-06-05T14:27:25Z",
 *   "merged_at": "2019-06-05T14:27:25Z",
 *   "merge_commit_sha": "defefd624ea2d036108768b9a658051fbae5b7a3",
 *   "assignee": null,
 *   "assignees": [],
 *   "requested_reviewers": [],
 *   "requested_teams": [],
 *   "labels": [],
 *   "milestone": null,
 *   "draft": false,
 *   "commits_url": "https://api.github.com/repos/square/retrofit/pulls/3114/commits",
 *   "review_comments_url": "https://api.github.com/repos/square/retrofit/pulls/3114/comments",
 *   "review_comment_url": "https://api.github.com/repos/square/retrofit/pulls/comments{/number}",
 *   "comments_url": "https://api.github.com/repos/square/retrofit/issues/3114/comments",
 *   "statuses_url": "https://api.github.com/repos/square/retrofit/statuses/c04ebd2566f1135bb46a0d39d2833133be6afa13",
 *   "head": {
 *     "label": "joelhandwell:patch-1",
 *     "ref": "patch-1",
 *     "sha": "c04ebd2566f1135bb46a0d39d2833133be6afa13",
 *     "user": {
 *       "login": "joelhandwell"
 *     },
 *     "repo": null
 *   },
 *   "base": {
 *     "label": "square:master",
 *     "ref": "master",
 *     "sha": "ce6e0d17005afd09c46e4fe6cf40643b0913c59d",
 *     "user": {
 *       "login": "square",
 *     },
 *     "repo": { }
 *   },
 *   "_links": {
 *     "self": {},
 *     "html": {},
 *     "issue": {},
 *     "comments": {},
 *     "review_comments": {},
 *     "review_comment": {},
 *     "commits": {},
 *     "statuses": {}
 *   },
 *   "author_association": "CONTRIBUTOR",
 *   "auto_merge": null,
 *   "active_lock_reason": null,
 *   "merged": true,
 *   "mergeable": null,
 *   "rebaseable": null,
 *   "mergeable_state": "unknown",
 *   "merged_by": {
 *     "login": "JakeWharton",
 *   },
 *   "comments": 1,
 *   "review_comments": 0,
 *   "maintainer_can_modify": false,
 *   "commits": 1,
 *   "additions": 2,
 *   "deletions": 1,
 *   "changed_files": 1
 * }
 * ```
 */
data class PullRequest(
    val id: Long,
    /**
     * PR number.
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
    val merged_at: String?
) {
    val isMerged: Boolean = merged != null && merged == true
    val prCreatedOn: Instant = created_at.toInstant()
    val prMergedOn: Instant? = merged_at?.toInstant()
}
