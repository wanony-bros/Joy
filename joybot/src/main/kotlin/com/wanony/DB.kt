package com.wanony

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DB {
    private val dataBaseName: String = getProperty("databaseName")

    fun getConnection() = Database.connect(db).also {
        org.jetbrains.exposed.sql.transactions.transaction {
            SchemaUtils.setSchema(Schema(dataBaseName))
        }
    }

    fun <T> transaction(statement: Transaction.() -> T) {
        Database.connect(db).also {
            org.jetbrains.exposed.sql.transactions.transaction {
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