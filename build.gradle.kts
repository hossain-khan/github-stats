import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // For build.gradle.kts (Kotlin DSL)
    kotlin("jvm") version "1.7.10"
    id("org.jmailen.kotlinter") version "3.12.0"
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


    testImplementation(kotlin("test"))
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