package dev.hossain.githubstats.service

import dev.hossain.githubstats.logging.Log
import dev.hossain.githubstats.model.timeline.TimelineEvent
import dev.hossain.githubstats.util.ErrorProcessor
import dev.hossain.githubstats.util.RateLimitHandler

/**
 * Collects all [TimelineEvent] for a pull request if max allowed per page is exceeded.
 */
class TimelineEventsPagerService constructor(
    private val githubApiService: GithubApiService,
    private val errorProcessor: ErrorProcessor,
    private val rateLimitHandler: RateLimitHandler = RateLimitHandler(),
    private val pageSize: Int = GithubApiService.DEFAULT_PAGE_SIZE,
) {
    /**
     * Provides all timeline events for [prNumber] by requesting multiple API requests if necessary.
     */
    suspend fun getAllTimelineEvents(
        repoOwner: String,
        repoId: String,
        prNumber: Int,
    ): List<TimelineEvent> {
        val allTimelineEvents = mutableListOf<TimelineEvent>()
        var pageNumber = 1

        do {
            val timelineEvents =
                try {
                    rateLimitHandler.executeWithRateLimit(
                        apiCall = {
                            githubApiService.timelineEvents(
                                owner = repoOwner,
                                repo = repoId,
                                issue = prNumber,
                                page = pageNumber,
                            )
                        },
                        errorProcessor = errorProcessor,
                    )
                } catch (exception: Exception) {
                    val errorInfo = errorProcessor.getDetailedError(exception)
                    throw errorInfo.exception
                }

            allTimelineEvents.addAll(timelineEvents)

            // Checks if we need to make next page request, because max per page was reached
            val nextPageRequestNeeded: Boolean = timelineEvents.size >= pageSize

            if (pageNumber > 1) {
                Log.d("Loaded ${timelineEvents.size} additional timeline events from page#$pageNumber.")
            }

            pageNumber++
        } while (nextPageRequestNeeded)

        return allTimelineEvents
    }
}
