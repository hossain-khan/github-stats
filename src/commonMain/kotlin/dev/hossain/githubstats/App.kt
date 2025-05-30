package dev.hossain.githubstats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Hello, Kotlin Multiplatform!")
        }
    }
}

// Desktop main function (if App.kt is also the entry point for desktop)
// This is not strictly needed if build.gradle.kts's compose.desktop.application.mainClass points to AppKt
// and the `application` plugin's mainClass also points to it.
// However, it's common to have an explicit main function for desktop.
// fun main() = androidx.compose.ui.window.application {
//     androidx.compose.ui.window.Window(onCloseRequest = ::exitApplication, title = "Github Stats") {
//         App()
//     }
// }
// For now, the main function will be handled by the generated AppKt by the application plugin
// and compose.desktop plugin if they correctly point to dev.hossain.githubstats.AppKt.
// The original Main.kt can be deleted or its content moved here if needed.
// The current Main.kt is in the default package. If it contains the desktop main, it needs to be adapted.
// Let's assume the Gradle plugins will handle creating the main entry point.
// The `mainClass.set("dev.hossain.githubstats.AppKt")` in build.gradle.kts should suffice for desktop.
