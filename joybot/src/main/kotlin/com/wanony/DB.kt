package com.wanony

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.transactions.transaction
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