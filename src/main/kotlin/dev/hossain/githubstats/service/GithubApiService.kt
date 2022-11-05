package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.PullRequestState
import dev.hossain.githubstats.model.timeline.TimelineEvent
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * REST API service for GitHub
 *
 * See [GitHub API Browser](https://docs.github.com/en/rest)
 */
interface GithubApiService {
    companion object {
        /**
         * Initial page number for GitHub API requests.
         */
        private const val DEFAULT_PAGE_NUMBER = 1

        /**
         * GitHub maximum resource item size for API requests.
         */
        const val DEFAULT_PAGE_SIZE = 100
    }

    /**
     * Lists details of a pull request by providing its number.
     *
     * https://docs.github.com/en/rest/pulls/pulls#get-a-pull-request
     */
    @GET("/repos/{owner}/{repo}/pulls/{pull_number}")
    suspend fun pullRequest(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") pullNumber: Int
    ): PullRequest

    /**
     * List pull requests
     *
     * https://docs.github.com/en/rest/pulls/pulls#list-pull-requests
     */
    @GET("/repos/{owner}/{repo}/pulls")
    suspend fun pullRequests(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        /**
         * Filter pulls by head user or head organization and branch name
         * in the format of `user:ref-name` or `organization:ref-name`.
         * For example: `github:new-script-format` or `octocat:test-branch`.
         */
        @Query("head") filter: String? = null,
        @Query("state") prState: String = PullRequestState.CLOSED.name.lowercase(),
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): List<PullRequest>

    /**
     * The Timeline events API can return different types of events triggered by timeline activity
     * in issues and pull requests.
     *
     * https://docs.github.com/en/rest/issues/timeline
     */
    @GET("repos/{owner}/{repo}/issues/{issue_number}/timeline")
    suspend fun timelineEvents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issue: Int,
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): List<TimelineEvent>

    /**
     * Lists all review comments for a pull request.
     * By default, review comments are in ascending order by ID.
     *
     * https://docs.github.com/en/rest/pulls/comments#list-review-comments-on-a-pull-request
     */
    @GET("/repos/{owner}/{repo}/pulls/{pull_number}/comments")
    suspend fun prReviewComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("pull_number") prNumber: Int,
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): List<CodeReviewComment>

    /**
     * Search issues and pull requests
     * Find issues by state and keyword. This method returns up to 100 results per page.
     * When searching for issues, you can get text match metadata for the issue title, issue body,
     * and issue comment body fields when you pass the text-match media type.
     * For more details about how to receive highlighted search results, see Text match metadata.
     *
     * https://docs.github.com/en/rest/search#search-issues-and-pull-requests
     */
    @GET("/search/issues")
    suspend fun searchIssues(
        /**
         * The query contains one or more search keywords and qualifiers. Qualifiers allow you to
         * limit your search to specific areas of GitHub.
         * The REST API supports the same qualifiers as the web interface for GitHub.
         * To learn more about the format of the query, see Constructing a search query.
         * See "Searching issues and pull requests" for a detailed list of qualifiers.
         *
         * Example: `repo:REPO`, `author:USER`, `is:pr`
         *          is:pr+repo:rails/rails+author:userlogin
         *
         *
         * https://docs.github.com/en/search-github/searching-on-github/searching-issues-and-pull-requests
         * @see SearchParams.toQuery
         */
        @Query("q", encoded = true) searchQuery: String,
        /**
         * Sorts the results of your query by the number of comments, reactions, reactions-+1, reactions--1,
         * reactions-smile, reactions-thinking_face, reactions-heart, reactions-tada, or interactions.
         * You can also sort results by how recently the items were created or updated, Default: best match
         */
        @Query("sort") sort: String = "created",
        /**
         * Determines whether the first search result returned is the highest number of matches (desc)
         * or lowest number of matches (asc). This parameter is ignored unless you provide sort.
         */
        @Query("order") order: String = "desc",
        @Query("page") page: Int = DEFAULT_PAGE_NUMBER,
        @Query("per_page") size: Int = DEFAULT_PAGE_SIZE
    ): IssueSearchResult
}
