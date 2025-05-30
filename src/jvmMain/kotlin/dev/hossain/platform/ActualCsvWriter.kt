package dev.hossain.platform

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter as underlyingCsvWriter // Alias to avoid name clash

actual class PlatformCsvWriterSession(
    private val writer: com.github.doyaaaaaken.kotlincsv.client.CsvWriterSession,
) {
    actual fun writeRow(vararg columns: Any?) {
        writer.writeRow(*columns)
    }

    actual fun writeRow(columns: List<Any?>) {
        writer.writeRow(columns)
    }
}

actual class PlatformCsvWriter {
    private val jvmCsvWriter = underlyingCsvWriter() // from com.github.doyaaaaaken.kotlincsv.dsl

    actual fun open(
        fileName: String,
        append: Boolean,
        writeBlock: PlatformCsvWriterSession.() -> Unit,
    ) {
        jvmCsvWriter.open(fileName, append) {
            // `this` is CsvWriterSession from the underlying library
            val session = PlatformCsvWriterSession(this)
            session.writeBlock()
        }
    }

    actual fun writeAll(
        rows: List<List<Any?>>,
        fileName: String,
        append: Boolean,
    ) {
        jvmCsvWriter.writeAll(rows, fileName, append = append)
    }
}

/**
 * Factory function for JVM platform.
 */
actual fun getCsvWriter(): PlatformCsvWriter {
    return PlatformCsvWriter()
}
