@file:JvmName("CreateDatabase")

package com.wanony.joy.data.util

import com.wanony.joy.data.dao.*
import com.wanony.joy.discord.getProperty
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    val url = getProperty<String>("databaseUrl")
    val driver = getProperty<String>("databaseDriver")
    val user = getProperty<String>("databaseUser")
    val password = getProperty<String>("databasePassword")
    val name = getProperty<String>("databaseName")

    println(
        """
        Connecting to database on url: $url
        Using driver: $driver
        With User: $user
        And Password: $password
        """.trimMargin()
    )
    Database.connect(
        url,
        driver = driver,
        user = user,
        password = password
    )

    val tables = listOf(
        Groups,
        LinkMembers,
        Links,
        LinkTags,
        Members,
        Tags,
        Users,
        Memes,
        RedditNotifications,
        InstagramNotifications,
        AuditingChannels,
        Guilds,
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.createDatabase(name)
        SchemaUtils.setSchema(Schema(getProperty("databaseName")))
        SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())

        Users.insertIgnore {
            it[id] = getProperty<Long>("slvinId")
            it[admin] = true
        }
        Users.insertIgnore {
            it[id] = getProperty<Long>("wanonyId")
            it[admin] = true
        }
        Memes.insertIgnore {
            it[meme] = "funni rat"
            it[content] = "Do you wanna see a funni rat?"
            it[addedBy] = getProperty<Long>("wanonyId")
        }
    }
}