package dev.hossain.githubstats.model

/**
 * Pull Request Review Comment that are comments on a portion of the Pull Request's diff.
 *
 * Sample JSON (simplified):
 * ```json
 * {
 *    "url": "https://api.github.com/repos/square/okhttp/pulls/comments/945941183",
 *    "pull_request_review_id": 1072931277,
 *    "id": 945941183,
 *    "node_id": "PRRC_kwDOAE6eHc44Yeq_",
 *    "diff_hunk": "okhttp.newCall(Request.Builder().url(\"https://example.com\").build()).enqueue(callback)",
 *    "path": "okhttp/src/jvmTest/java/okhttp3/DispatcherCleanupTest.kt",
 *    "position": null,
 *    "original_position": 32,
 *    "commit_id": "c7f1e8e730c04d750d2ad66c93a3eddf3a39b91c",
 *    "original_commit_id": "8b4db8381c057e26cb48d176f17ad2cb1f709775",
 *    "user": {
 *      "login": "JakeWharton",
 *    },
 *    "body": "Probably needs MockWebServer so as not to be dependent on the internet.",
 *    "created_at": "2022-08-15T16:48:57Z",
 *    "updated_at": "2022-08-15T16:49:17Z",
 *    "html_url": "https://github.com/square/okhttp/pull/7415#discussion_r945941183",
 *    "pull_request_url": "https://api.github.com/repos/square/okhttp/pulls/7415",
 *    "author_association": "MEMBER",
 *    "reactions": {},
 *    "start_line": null,
 *    "original_start_line": null,
 *    "start_side": null,
 *    "line": null,
 *    "original_line": 32,
 *    "side": "RIGHT"
 * }
 * ```
 */
data class CodeReviewComment(
    val user: User,
    val body: String,
    val id: Long,
    val pull_request_url: String,
    val html_url: String,
    val pull_request_review_id: String,
    val commit_id: String,
    val original_commit_id: String,
    val created_at: String,
    val updated_at: String,
    val author_association: String,
)
