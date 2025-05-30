package dev.hossain.platform

/**
 * Expected functionality for formatting data into a text-based table.
 * This abstracts libraries like Picnic.
 */
expect class PlatformTableFormatter {
    /**
     * Formats a list of rows into a table string.
     * Each inner list represents a row, and its elements are the cells.
     * The first list in `rows` can be treated as the header.
     */
    fun formatTable(rows: List<List<Any?>>): String

    // You might add more specific methods if the Picnic API exposed more granular control
    // that you were using, e.g., cell styling, alignment, etc.
    // For now, a simple table formatter from a list of rows.
}

/**
 * Factory to get an instance of the platform-specific table formatter.
 */
expect fun getTableFormatter(): PlatformTableFormatter
