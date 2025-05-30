import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary) // Apply Android Library plugin for androidTarget
    alias(libs.plugins.androidApplication) apply false // For potential Android app modules
    alias(libs.plugins.jetbrainsCompose)
    // No separate compose.compiler plugin needed with modern org.jetbrains.compose plugin
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.dokka)
    // application // Removed: Conflicts with com.android.library on root project. Desktop is handled by Compose plugin.
}

group = "dev.hossain.githubstats"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google() // For Android dependencies
    maven("https.maven.pkg.jetbrains.space/public/p/compose/dev") // For Compose Multiplatform
}

kotlin {
    // Refer to libs.versions.toml for Kotlin version (e.g., 1.9.23)
    // and Compose versions (e.g., org.jetbrains.compose plugin 1.6.2)

    androidTarget {
        // Configure Android target. Specifics like compileSdk, minSdk, namespace
        // will be in the consuming Android app module's build.gradle.kts or AndroidManifest.xml.
        // This block configures the KMP library aspects for Android.
        compilations.all {
            kotlinOptions.jvmTarget = "1.8" // Or higher, matching Android project's requirements
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // JetBrains Compose
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3) // For Material Design 3
                implementation(compose.materialIconsExtended) // Optional: for more icons
                implementation(compose.components.resources) // For common resources
                implementation(compose.components.uiToolingPreview) // For previews

                // Kotlinx Libraries
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)

                // Networking (OkHttp is KMP-friendly for its core, but Retrofit is not)
                // Do not use BOM directly in commonMain for non-KMP specific BOMs.
                // implementation(libs.squareup.okhttp.bom) 
                implementation(libs.squareup.okhttp) // Version from libs.versions.toml will be used
                implementation(libs.squareup.okhttp.logging.interceptor) // Version from libs.versions.toml
                // NOTE: Retrofit is JVM-only. Consider Ktor for KMP: implementation(libs.ktor.client.core)

                // CSV Parsing
                // NOTE: kotlin-csv-jvm is JVM-only. Search for a KMP alternative or use expect/actual.
                // implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")

                // Data Serialization (Moshi is JVM-only)
                // NOTE: Moshi is JVM-only. Use kotlinx-serialization for KMP: implementation(libs.kotlinx.serialization.json)

                // Dependency Injection (Koin has KMP support)
                implementation(platform("io.insert-koin:koin-bom:${libs.versions.koin.get()}")) // Apply Koin BOM with explicit string
                implementation(libs.koin.core) // Version will be supplied by koin-bom

                // UI Helpers (Picnic, progressbar are JVM-only)
                // NOTE: Picnic is JVM-only.
                // NOTE: me.tongfei:progressbar is JVM-only.
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.mockk.common) // MockK common artifact for KMP

                // NOTE: Truth, MockWebServer, JUnit5 are primarily JVM test tools.
                // For KMP, use kotlin.test assertions and platform-specific runners or tools.
                // implementation("com.google.truth:truth:1.4.4")
                // implementation("com.squareup.okhttp3:mockwebserver:4.12.0")
                // implementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.23")
                // implementation("org.junit.jupiter:junit-jupiter:5.12.2")
            }
        }

        val androidMain by getting {
            dependencies {
                // Android-specific KMP dependencies can go here if needed
                // e.g., implementation(libs.androidx.core.ktx)
                // Actual Android app dependencies (AppCompat, Material for Android) go in the app module.
            }
        }

        val iosMain by creating { // Combines all iOS targets
            dependsOn(commonMain)
            // The specific iOS source sets (iosX64Main, iosArm64Main, iosSimulatorArm64Main)
            // will automatically depend on iosMain by convention.
            // No need to declare these dependencies explicitly here.
            dependencies {
                // iOS-specific KMP dependencies can be added here
                // if they are common to all iOS targets.
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs) // For desktop-specific Compose APIs
            }
        }
    }

    jvmToolchain(17) // Set JDK version for JVM targets
}

// Android specific configuration now that com.android.library is applied
android {
    namespace = "dev.hossain.githubstats" // Replace with your desired namespace
    compileSdk = 34 // Or your target compile SDK

    // Min SDK can be set here or often in androidTarget for KMP
    defaultConfig {
        minSdk = 24
    }

    // Lint options, build features, etc., can also be configured here if needed
    // For KMP library, often this block is minimal.
}

// The standard `application` plugin was removed due to conflict with `com.android.library`.
// The main class for desktop is configured via `compose.desktop` block below.
// application {
//    mainClass.set("dev.hossain.githubstats.AppKt") // This block is no longer needed.
// }

// Configuration for JetBrains Compose for Desktop
compose.desktop {
    application {
        mainClass = "dev.hossain.githubstats.AppKt" // Entry point for desktop app

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "GithubStats"
            packageVersion = "1.0.0"
            // vendor = "YourCompany" // Optional
            // description = "My Awesome KMP App" // Optional
        }
    }
}

// Kotlinter (linter)
// Configuration should be picked up automatically or can be customized here if needed
// kotlinter { ... }

// Dokka (documentation engine)
// Basic configuration. More specific KMP setup might be needed for full multiplatform docs.
// tasks.dokkaHtml {
//    outputDirectory.set(buildDir.resolve("dokka"))
// }

// Notes for creating/updating gradle/libs.versions.toml:
// [versions]
// kotlin = "1.9.23"
// jetbrainsCompose = "1.6.2" // Plugin version for org.jetbrains.compose
// androidGradlePlugin = "8.2.0" // Or your project's AGP version
// kotlinter = "5.1.0"
// dokka = "1.9.20"
// kotlinxCoroutines = "1.10.2" // Example, use latest
// kotlinxDatetime = "0.6.2"
// okhttp = "4.12.0"
// koin = "4.0.4" // Check Koin KMP version, e.g., koin-core
// mockk = "1.14.2" // Check MockK KMP version
// ktor = "2.3.12" // Example if using Ktor
// kotlinxSerializationJson = "1.7.0" // Example if using kotlinx-serialization
// androidxCoreKtx = "1.13.1" // Example for Android

// [libraries]
// kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
// kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinxCoroutines" }
// kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }
// squareup-okhttp-bom = { module = "com.squareup.okhttp3:okhttp-bom", version.ref = "okhttp" }
// squareup-okhttp = { module = "com.squareup.okhttp3:okhttp" }
// squareup-okhttp-logging-interceptor = { module = "com.squareup.okhttp3:logging-interceptor" }
// koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" } // Ensure this is the KMP artifact
// mockk-common = { module = "io.mockk:mockk-common", version.ref = "mockk" }
// androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidxCoreKtx"}
// ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor"}
// kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerializationJson"}


// [plugins]
// kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
// androidApplication = { id = "com.android.application", version.ref = "androidGradlePlugin" }
// jetbrainsCompose = { id = "org.jetbrains.compose", version.ref = "jetbrainsCompose" }
// kotlinter = { id = "org.jmailen.kotlinter", version.ref = "kotlinter" }
// dokka = { id = "org.jetbrains.dokka", version.ref = "dokka" }

// Make sure to create/update the libs.versions.toml file accordingly.
// The build script now uses aliases (e.g., libs.plugins.kotlinMultiplatform).
// These aliases must be defined in the TOML file.