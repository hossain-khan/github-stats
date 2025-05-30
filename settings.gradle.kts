pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    // Removed Foojay plugin declaration
}

// Removed apply(plugin = "org.gradle.toolchains.foojay-resolver-convention")

// Removed toolchainManagement block for Foojay
// toolchainManagement {
//    jvm {
//        javaRepositories {
//            repository("foojay") {
//                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
//            }
//        }
//    }
// }

rootProject.name = "github-stats"

// Required for Compose Multiplatform with Kotlin 2.x
// https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-setup.html#kotlin-configuration
// enableFeaturePreview("GRADLE_METADATA") // This feature seems to be removed or renamed in Gradle 8.x
// enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // Not strictly needed now, but good for future
System.setProperty("kotlin.experimental.tryK2", "true")
System.setProperty("org.jetbrains.compose.experimental.uikit.enabled", "true")
