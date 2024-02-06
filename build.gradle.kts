import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // For build.gradle.kts (Kotlin DSL)
    // https://kotlinlang.org/docs/releases.html#release-details
    kotlin("jvm") version "1.9.22"
    id("org.jmailen.kotlinter") version "4.2.0"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // https://github.com/Kotlin/kotlinx-datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // https://square.github.io/okhttp/
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // https://square.github.io/retrofit/
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // https://github.com/JakeWharton/picnic
    implementation("com.jakewharton.picnic:picnic:0.7.0")

    // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.squareup.moshi:moshi-adapters:1.15.0")
    // ksp("com.squareup.moshi:moshi-kotlin-codegen:1.14.0") // Not needed yet.

    // https://github.com/doyaaaaaken/kotlin-csv
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3") //for JVM platform

    // Koin Core features - https://insert-koin.io/
    implementation("io.insert-koin:koin-core:3.5.3")

    // ASCII Progress Bar https://github.com/ctongfei/progressbar
    implementation("me.tongfei:progressbar:0.10.0")

    //
    // =======================
    // Unit Test Dependencies
    // =======================
    //
    testImplementation(kotlin("test"))
    testImplementation("com.google.truth:truth:1.4.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    // MockK - https://mockk.io/ : don't use 1.13.8 due to
    // https://github.com/mockk/mockk/issues/1168#issuecomment-1823071494
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
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