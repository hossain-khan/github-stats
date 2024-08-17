import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // For build.gradle.kts (Kotlin DSL)
    // https://kotlinlang.org/docs/releases.html#release-details
    kotlin("jvm") version "2.0.10"
    id("org.jmailen.kotlinter") version "4.4.1"

    // Dokka - API documentation engine for Kotlin
    // https://github.com/Kotlin/dokka
    id("org.jetbrains.dokka") version "1.9.20"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // https://github.com/Kotlin/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

    // https://square.github.io/okhttp/
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // https://square.github.io/retrofit/
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")

    // https://github.com/JakeWharton/picnic
    implementation("com.jakewharton.picnic:picnic:0.7.0")

    // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.moshi:moshi-adapters:1.15.1")
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0") // Not needed yet.

    // https://github.com/doyaaaaaken/kotlin-csv
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0") //for JVM platform

    // Koin Core features - https://insert-koin.io/
    implementation("io.insert-koin:koin-core:3.5.6")

    // ASCII Progress Bar https://github.com/ctongfei/progressbar
    implementation("me.tongfei:progressbar:0.10.1")

    //
    // =======================
    // Unit Test Dependencies
    // =======================
    //
    testImplementation(kotlin("test"))
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    // MockK - https://mockk.io/ : don't use 1.13.8 due to
    // https://github.com/mockk/mockk/issues/1168#issuecomment-1823071494
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    /**
     * https://kotlinlang.org/docs/compiler-reference.html#jvm-target-version
     */
    kotlinOptions.jvmTarget = "17"
}

kotlin {
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}