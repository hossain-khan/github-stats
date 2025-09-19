import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask

plugins {
    // For build.gradle.kts (Kotlin DSL)
    // https://kotlinlang.org/docs/releases.html#release-details
    kotlin("jvm") version "2.2.20"
    id("org.jmailen.kotlinter") version "5.2.0"

    // Dokka - API documentation engine for Kotlin
    // https://github.com/Kotlin/dokka
    // https://kotlinlang.org/docs/dokka-migration.html
    id("org.jetbrains.dokka") version "2.0.0"

    // SQLDelight plugin for database code generation
    // https://sqldelight.github.io/sqldelight/2.1.0/
    id("app.cash.sqldelight") version "2.1.0"

    // https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project
    // id("com.google.devtools.ksp") version "1.9.20-1.0.6" // Not needed yet.
    application
}

group = "dev.hossain.githubstats"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://github.com/Kotlin/kotlinx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    // https://github.com/Kotlin/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // https://square.github.io/okhttp/
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:5.1.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // https://square.github.io/retrofit/
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")

    // https://github.com/JakeWharton/picnic
    implementation("com.jakewharton.picnic:picnic:0.7.0")

    // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.moshi:moshi-adapters:1.15.2")
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0") // Not needed yet.

    // https://github.com/doyaaaaaken/kotlin-csv
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0") //for JVM platform

    // Koin Core features - https://insert-koin.io/
    implementation("io.insert-koin:koin-core:4.1.1")

    // ASCII Progress Bar https://github.com/ctongfei/progressbar
    implementation("me.tongfei:progressbar:0.10.1")

    // SQLDelight for database operations and PostgreSQL driver
    // https://sqldelight.github.io/sqldelight/2.1.0/
    implementation("app.cash.sqldelight:runtime:2.1.0")
    implementation("app.cash.sqldelight:coroutines-extensions:2.1.0")
    implementation("app.cash.sqldelight:jdbc-driver:2.1.0")
    implementation("org.postgresql:postgresql:42.7.8")

    //
    // =======================
    // Unit Test Dependencies
    // =======================
    //
    testImplementation(kotlin("test"))
    testImplementation("com.google.truth:truth:1.4.5")
    testImplementation("com.squareup.okhttp3:mockwebserver:5.1.0")
    // MockK - https://mockk.io/ : don't use 1.13.8 due to
    // https://github.com/mockk/mockk/issues/1168#issuecomment-1823071494
    testImplementation("io.mockk:mockk:1.14.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.2.20")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(17)

    compilerOptions {
        /**
         * - https://kotlinlang.org/docs/compiler-reference.html#jvm-target-version
         * - https://kotlinlang.org/docs/gradle-compiler-options.html#target-the-jvm
         */
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

application {
    mainClass.set("MainKt")
}

// SQLDelight configuration for PostgreSQL
sqldelight {
    databases {
        create("GitHubStatsDatabase") {
            packageName.set("dev.hossain.githubstats.cache.database")
            dialect("app.cash.sqldelight:postgresql-dialect:2.1.0")
            // deriveSchemaFromMigrations.set(true) // Remove this for direct .sq files
        }
    }
}

// Exclude generated files from kotlinter checks
// https://github.com/jeremymailen/kotlinter-gradle/issues/242#issuecomment-2720690736
tasks.withType<ConfigurableKtLintTask>().configureEach {
    val buildDirectory = layout.buildDirectory
    exclude {
        it.file.startsWith(buildDirectory.get().asFile)
    }
}