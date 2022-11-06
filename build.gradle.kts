import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // For build.gradle.kts (Kotlin DSL)
    // https://kotlinlang.org/docs/releases.html#release-details
    kotlin("jvm") version "1.7.10"
    id("org.jmailen.kotlinter") version "3.12.0"
    // https://kotlinlang.org/docs/ksp-quickstart.html#use-your-own-processor-in-a-project
    // id("com.google.devtools.ksp") version "1.7.10-1.0.6" // Not needed yet.
    application
}

group = "dev.hossain.githubstats"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://github.com/Kotlin/kotlinx.coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // https://github.com/Kotlin/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // https://square.github.io/okhttp/
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // https://square.github.io/retrofit/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // https://github.com/JakeWharton/picnic
    implementation("com.jakewharton.picnic:picnic:0.6.0")

    // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi:1.14.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("com.squareup.moshi:moshi-adapters:1.14.0")
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0") // Not needed yet.

    // https://github.com/doyaaaaaken/kotlin-csv
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.6.0") //for JVM platform

    // Koin Core features - https://insert-koin.io/
    implementation("io.insert-koin:koin-core:3.2.2")

    // ASCII Progress Bar https://github.com/ctongfei/progressbar
    implementation("me.tongfei:progressbar:0.9.5")

    //
    // =======================
    // Unit Test Dependencies
    // =======================
    //
    testImplementation(kotlin("test"))
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.10")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}