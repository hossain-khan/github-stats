package dev.hossain.githubstats.model

/**
 * Pull Request Review Comment.
 * Pull Request Review Comments are comments on a portion of the Pull Request's diff.
 *
 * Sample JSON (simplified):
 * ```json
 * {
 *    "url": "https://api.github.com/repos/square/okhttp/pulls/comments/945941183",
 *    "pull_request_review_id": 1072931277,
 *    "id": 945941183,
 *    "node_id": "PRRC_kwDOAE6eHc44Yeq_",
 *    "diff_hunk": "@@ -0,0 +1,36 @@\n+/*\n+ * Copyright (C) 2022 Square, Inc.\n+ *\n+ * Licensed under the Apache License, Version 2.0 (the \"License\");\n+ * you may not use this file except in compliance with the License.\n+ * You may obtain a copy of the License at\n+ *\n+ *      http://www.apache.org/licenses/LICENSE-2.0\n+ *\n+ * Unless required by applicable law or agreed to in writing, software\n+ * distributed under the License is distributed on an \"AS IS\" BASIS,\n+ * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n+ * See the License for the specific language governing permissions and\n+ * limitations under the License.\n+ */\n+package okhttp3\n+\n+import java.io.IOException\n+import org.junit.jupiter.api.Test\n+\n+class DispatcherCleanupTest {\n+  @Test\n+  fun testFinish() {\n+    val okhttp = OkHttpClient()\n+    val callback = object : Callback {\n+      override fun onFailure(call: Call, e: IOException) {}\n+      override fun onResponse(call: Call, response: Response) {\n+        response.close()\n+      }\n+    }\n+    repeat(10_000) {\n+      okhttp.newCall(Request.Builder().url(\"https://example.com\").build()).enqueue(callback)",
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
data class ReviewComment(
    val id: Long,
    val pull_request_review_id: String,
    val commit_id: String,
    val original_commit_id: String,
    val user: User,
    val body: String,
    val created_at: String,
    val updated_at: String,
    val html_url: String,
    val pull_request_url: String,
    val author_association: String
)
