package dev.hossain.githubstats.cache

/**
 * Represents the type of database used for response caching.
 */
enum class DatabaseType {
    SQLITE,
    POSTGRESQL,
    NONE,
    ;

    companion object {
        fun fromString(type: String?): DatabaseType =
            when (type?.trim()?.uppercase()) {
                "SQLITE", "SQLITE3" -> SQLITE
                "POSTGRESQL", "POSTGRES" -> POSTGRESQL
                "NONE", "DISABLED", "FALSE" -> NONE
                else -> NONE
            }
    }
}
