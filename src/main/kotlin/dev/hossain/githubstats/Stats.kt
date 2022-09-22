package dev.hossain.githubstats

import java.io.File
import java.util.*

fun main(args: Array<String>) {
    println("Hello World!")

    val propertiesFile = File("local.properties")
    if (propertiesFile.exists()) {
        val properties = Properties()
        properties.load(propertiesFile.inputStream())
        println(properties.containsKey("token"))
    } else {
        println("No properties file")
    }

    println("Program arguments: ${args.joinToString()}")
}
