package dev.hossain.platform

/**
 * Expected functionality for file system operations.
 */
expect class PlatformFile(pathname: String) {
    fun writeText(text: String)
    fun readText(): String
    fun getAbsolutePath(): String
    fun getName(): String
    fun exists(): Boolean
    fun mkdirs(): Boolean
    fun isFile(): Boolean
    fun isDirectory(): Boolean
    // Add other necessary file operations like delete, listFiles, etc.
}

/**
 * Utility to create directory path for a file if it doesn't exist.
 * This is not an expect itself but uses `expect PlatformFile`.
 */
fun ensureDirectoryExists(file: PlatformFile) {
    // This is a simplified version. A real implementation might need to go up the path.
    // For now, assuming the parent directory structure is handled by `mkdirs` on the file's parent.
    // This function might be better placed in actual implementations or a common utility class using PlatformFile.
    // However, the concept of needing to create parent dirs is common.
    // Let's refine this: perhaps PlatformFile needs a getParentFile() method.
    // For now, this is a placeholder for where such logic might go.
    // A simple `PlatformFile(filePath).getParentFile()?.mkdirs()` would be more direct if getParentFile exists.
}

/**
 * Provides a platform-specific way to get a file path for a report.
 * This might involve specific directories on each platform.
 */
expect fun getReportPath(filename: String): String

/**
 * Provides a platform-specific way to get a path for a resource,
 * which might be bundled differently on each platform.
 */
expect fun getResourcePath(resourceName: String): String
