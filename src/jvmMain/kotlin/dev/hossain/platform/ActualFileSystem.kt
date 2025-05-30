package dev.hossain.platform

import java.io.File
import java.io.FileNotFoundException

actual class PlatformFile actual constructor(private val jvmPathname: String) {
    private val file = File(jvmPathname)

    actual fun writeText(text: String) {
        // Ensure parent directories exist before writing
        file.parentFile?.mkdirs()
        file.writeText(text)
    }

    actual fun readText(): String {
        if (!file.exists()) throw FileNotFoundException("File not found: $jvmPathname")
        return file.readText()
    }

    actual fun getAbsolutePath(): String = file.absolutePath
    actual fun getName(): String = file.name
    actual fun exists(): Boolean = file.exists()
    actual fun mkdirs(): Boolean = file.mkdirs()
    actual fun isFile(): Boolean = file.isFile
    actual fun isDirectory(): Boolean = file.isDirectory
    // Add other necessary file operations here, like delete, listFiles, etc.
}

/**
 * Provides a platform-specific way to get a file path for a report.
 * On JVM, this can simply be a relative path.
 */
actual fun getReportPath(filename: String): String {
    // This could be made more sophisticated, e.g. user.home, specific app dir, etc.
    return filename // Assuming reports are created in the current working directory or a subdirectory
}

/**
 * Provides a platform-specific way to get a path for a resource.
 * On JVM, resources are typically loaded from the classpath.
 * This function might return a path if the resource is copied to file system,
 * or it might be unused if resources are read as streams directly.
 * For simplicity, returning a path assuming it's relative or will be handled appropriately.
 */
actual fun getResourcePath(resourceName: String): String {
    // This is tricky for JVM resources from classpath.
    // A common pattern is to copy the resource to a temp file or access via classloader.
    // For now, returning a simple name, assuming it might be a file path.
    // Proper resource handling in KMP often involves expect/actual for loading streams.
    val classLoader = Thread.currentThread().contextClassLoader
    val resourceUrl = classLoader.getResource(resourceName)
    return resourceUrl?.toURI()?.path ?: throw FileNotFoundException("Resource not found: $resourceName")
}
