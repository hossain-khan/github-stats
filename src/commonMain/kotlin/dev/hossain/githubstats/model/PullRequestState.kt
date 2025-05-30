package dev.hossain.githubstats.model

// import com.squareup.moshi.Json // Removed Moshi import

/**
 * State for GitHub PR.
 * Serialized names like "open", "closed" will be handled by kotlinx.serialization
 * using @SerialName if needed, or by matching enum names (case-insensitive).
 */
enum class PullRequestState {
    // @Json(name = "open") // Annotation removed
    OPEN,

    // @Json(name = "closed") // Annotation removed
    CLOSED,

    // @Json(name = "all") // Annotation removed
    ALL,
}
