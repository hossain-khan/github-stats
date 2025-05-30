package dev.hossain.githubstats.service

import dev.hossain.githubstats.model.CodeReviewComment
import dev.hossain.githubstats.model.IssueSearchResult
import dev.hossain.githubstats.model.PullRequest
import dev.hossain.githubstats.model.User
import dev.hossain.githubstats.model.timeline.TimelineEvent

/**
 * Actual JVM implementation for [GithubApiService].
 * This is a placeholder and should be implemented using Ktor client.
 */
actual class ActualGithubApiService : GithubApiService {
    override suspend fun pullRequest(owner: String, repo: String, pullNumber: Int): PullRequest {
        throw NotImplementedError("Ktor client not yet implemented for JVM - pullRequest")
    }

    override suspend fun pullRequests(
        owner: String,
        repo: String,
        filter: String?,
        prState: String,
        page: Int,
        size: Int,
    ): List<PullRequest> {
        throw NotImplementedError("Ktor client not yet implemented for JVM - pullRequests")
    }

    override suspend fun timelineEvents(
        owner: String,
        repo: String,
        issue: Int,
        page: Int,
        size: Int,
    ): List<TimelineEvent> {
        throw NotImplementedError("Ktor client not yet implemented for JVM - timelineEvents")
    }

    override suspend fun prSourceCodeReviewComments(
        owner: String,
        repo: String,
        prNumber: Int,
        page: Int,
        size: Int,
    ): List<CodeReviewComment> {
        throw NotImplementedError("Ktor client not yet implemented for JVM - prSourceCodeReviewComments")
    }

    override suspend fun searchIssues(
        searchQuery: String,
        sort: String,
        order: String,
        page: Int,
        size: Int,
    ): IssueSearchResult {
        throw NotImplementedError("Ktor client not yet implemented for JVM - searchIssues")
    }

    override suspend fun topContributors(owner: String, repo: String, itemPerPage: Int): List<User> {
        throw NotImplementedError("Ktor client not yet implemented for JVM - topContributors")
    }
}

// Koin binding for jvmMain would provide this:
// module {
//     single<GithubApiService> { ActualGithubApiService() }
// }
// This binding should go into a jvmMain Koin module definition file.
// For now, just defining the class. Koin setup for actuals will be a later step if needed.
