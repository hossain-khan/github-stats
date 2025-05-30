package dev.hossain.platform

/**
 * Expected functionality for writing CSV files.
 */
expect class PlatformCsvWriter {
    fun open(
        fileName: String,
        append: Boolean = false,
        writeBlock: PlatformCsvWriterSession.() -> Unit,
    )

    fun writeAll(
        rows: List<List<Any?>>,
        fileName: String,
        append: Boolean = false,
    )
}

/**
 * Represents a session for writing rows to a CSV file.
 */
expect class PlatformCsvWriterSession {
    fun writeRow(vararg columns: Any?)
    fun writeRow(columns: List<Any?>)
    // Potentially add close() if manual resource management is needed,
    // though the `open` block pattern often handles this.
}

/**
 * Factory function to get an instance of the platform-specific CSV writer.
 */
expect fun getCsvWriter(): PlatformCsvWriter
