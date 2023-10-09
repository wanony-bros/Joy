package com.wanony.joy.data.dao

import com.wanony.joy.discord.getProperty
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import javax.sql.DataSource

object DB {
    private val dataBaseName: String = getProperty("databaseName")

    fun <T> transaction(statement: Transaction.() -> T): T {
        Database.connect(db).also {
            return org.jetbrains.exposed.sql.transactions.transaction {
                SchemaUtils.setSchema(Schema(dataBaseName))
                statement()
            }
        }
    }

    private var db: DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = getProperty("databaseUrl")
        config.driverClassName = getProperty("databaseDriver")
        config.username = getProperty("databaseUser")
        config.password = getProperty("databasePassword")

        return HikariDataSource(config)
    }
}