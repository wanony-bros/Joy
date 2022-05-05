@file:JvmName("CreateDatabase")

package com.wanony.utils

import com.wanony.dao.*
import com.wanony.getProperty
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
    )

    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.createDatabase(name)
        SchemaUtils.setSchema(Schema("joy"))
        SchemaUtils.createMissingTablesAndColumns(*tables.toTypedArray())

        Users.insert {
            it[id] = getProperty<Long>("slvinId")
            it[admin] = true
        }
        Users.insert {
            it[id] = getProperty<Long>("wanonyId")
            it[admin] = true
        }
    }
}