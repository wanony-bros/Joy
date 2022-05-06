@file:JvmName("SetupProperties")

package com.wanony.utils

import java.nio.file.Paths
import java.util.*
import kotlin.system.exitProcess

fun main() {
    val file = Paths.get("src", "main", "resources", "misc.properties").toFile()
    var acceptBlanks = false
    if (!file.exists()) {
        if (!file.createNewFile()) {
            println("Couldn't create resource misc.properties under src/main/resource")
            exitProcess(1)
        }

    } else {
        print("misc.properties already exists, would you like to reconfigure? (Y/N): ")
        if (!readLine().equals("y", true)) {
            println("misc.properties is already configured, exiting!")
            exitProcess(1)
        }

        acceptBlanks = true
    }

    val properties = Properties().apply { load(file.inputStream()) }

    println("We will now go through the required properties${if (acceptBlanks) " (leave blank to use current value)" else ""}: ")
    val token = askForValue("What is your Discord api token?", acceptBlanks)
    token.ifNotBlank { properties.setProperty("discordAPIToken", it) }
    val databaseUrl = askForValue("What is the JDBC url for the database?", acceptBlanks)
    databaseUrl.ifNotBlank { properties.setProperty("databaseUrl", it) }
    val databaseDriver = askForValue("What is the fully qualified classname for the database driver?", acceptBlanks)
    databaseDriver.ifNotBlank { properties.setProperty("databaseDriver", it) }
    val databaseUser = askForValue("What is the database user to connect with?", acceptBlanks)
    databaseUser.ifNotBlank { properties.setProperty("databaseUser", it) }
    val databasePassword = askForValue("What is the database password for that user?", acceptBlanks)
    databasePassword.ifNotBlank { properties.setProperty("databasePassword", it) }
    val databaseName = askForValue("What is the name of the database to use?", acceptBlanks)
    databaseName.ifNotBlank { properties.setProperty("databaseName", databaseName) }
    properties.setProperty("slvinId", "492769957508284447")
    properties.setProperty("wanonyId", "107215130785243136")
    val testGuildId = askForValue("What is the id of the test guild being used?", acceptBlanks)
    testGuildId.ifNotBlank { properties.setProperty("testGuild", it) }
    val suggestChannel = askForValue("What is the id of the suggest channel?", acceptBlanks)
    suggestChannel.ifNotBlank { properties.setProperty("suggestChannel", it) }

    val outStream = file.outputStream()
    properties.store(outStream, "Misc properties")
    outStream.close()

    println("Properties are not configured!")
}

private fun basicValidation(acceptBlanks: Boolean) : (String) -> Boolean = { str -> str.isNotBlank() || acceptBlanks }

private fun String?.ifNotBlank(f: (String) -> Unit) = if (!isNullOrBlank()) f(this) else Unit

fun askForValue(
    prompt: String,
    acceptBlanks: Boolean,
    validation: (String) -> Boolean = basicValidation(acceptBlanks)
): String? {
    mutableMapOf<String, MutableList<String>>().getOrPut("id", ::mutableListOf).add("link")

    var value: String?
    while (true) {
        println("$prompt ")
        value = readLine()
        if (value == null) {
            println("Couldn't read from stdin input stream, exiting.")
            exitProcess(1)
        }
        if (validation(value)) {
            break
        }

        println("Value does not pass validation please try again.")
    }

    return value
}