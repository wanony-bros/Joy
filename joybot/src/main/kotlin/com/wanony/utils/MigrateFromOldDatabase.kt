@file:JvmName("MigrateFromOldDatabase")

package com.wanony.utils

import com.wanony.dao.*
import com.wanony.getProperty
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import javax.sql.DataSource

object OldDB {

    fun <T> transaction(statement: Transaction.() -> T): T {
        Database.connect(db).also {
            return org.jetbrains.exposed.sql.transactions.transaction {
                statement()
            }
        }
    }

    var db: DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:mysql://localhost:3308"
        config.username = "com.mysql.jdbc.Driver"
        config.password = "aRootPassword"
        config.driverClassName = "com.mysql.jdbc.Driver"

        return HikariDataSource(config)

    }
}

object NewDB {

    fun <T> transaction(statement: Transaction.() -> T): T {
        Database.connect(db).also {
            return org.jetbrains.exposed.sql.transactions.transaction {
                statement()
            }
        }
    }

    var db: DataSource = connect()

    private fun connect(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = getProperty<String>("databaseUrl")
        config.username = getProperty<String>("databaseUser")
        config.password = getProperty<String>("databasePassword")
        config.driverClassName = getProperty<String>("databaseDriver")

        return HikariDataSource(config)

    }
}

fun main() {

    // migrate in order of things that have no conflict with new measures
    fun migrateUsersAndModerators() {
        // first we move all users from users table to new users table
        val selectUsers = OldDB.db.connection.createStatement().executeQuery(
            "SELECT UserId, Xp, Cont FROM users;"
        )
        NewDB.transaction {
            while (selectUsers.next()) {
                Users.insertIgnore {
                    it[Users.id] = selectUsers.getInt("UserId").toLong()
                    it[Users.xp] = selectUsers.getInt("Xp")
                    it[Users.contribution] = selectUsers.getInt("Cont")
                    it[Users.admin] = false
                }
            }
        }
        selectUsers.close()
        // second, we update the admin flag for moderator users
        val selectMods = OldDB.db.connection.createStatement().executeQuery(
            "SELECT UserId FROM moderators;"
        )
        NewDB.transaction {
            while (selectMods.next()) {
                val modId = selectMods.getInt("UserId").toLong()
                Users.update({ Users.id eq modId }) {
                    it[Users.admin] = true
                }
            }
        }

        println("Finished migrating Users and Moderators...")
    }

    fun migrateMemes() {
        // direct migration
        val selectMemes = OldDB.db.connection.createStatement().executeQuery(
            "SELECT CommandName, Command, AddedBy FROM custom_commands;"
        )
        NewDB.transaction {
            while (selectMemes.next()) {
                Memes.insertIgnore {
                    it[Memes.meme] = selectMemes.getString("CommandName")
                    it[Memes.content] = selectMemes.getString("Command")
                    it[Memes.addedBy] = selectMemes.getInt("AddedBy").toLong()
                }
            }
        }

        println("Finished migrating Memes...")
    }

    fun migrateGuilds() {
        // near direct, just GuildId and TimerLimit
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT Guild, TimerLimit FROM guilds;"
        )

        NewDB.transaction {
            while (selectGuilds.next()) {
                // TODO make the guild DAO thing
            }
        }
    }

    fun migrateInstagramAndInstagramChannels() {
        // merged into one table
        val selectInstagram = OldDB.db.connection.createStatement().executeQuery(
            "SELECT i.Instagram, ic.ChannelId FROM instagram i JOIN instagram_channels ic WHERE i.InstagramId = ic.InstagramId;"
        )

        NewDB.transaction {
            while (selectInstagram.next()) {
                InstagramNotifications.insertIgnore {
                    it[InstagramNotifications.channelId] = selectInstagram.getString("ChannelId")
                    it[InstagramNotifications.userId] = selectInstagram.getString("Instagram")
                }
            }
        }

    }

    fun migrateRedditAndRedditChannels() {
        // merged into one table
        val selectReddit = OldDB.db.connection.createStatement().executeQuery(
            "SELECT r.RedditName, rc.ChannelId FROM reddit r JOIN reddit_channels rc WHERE r.RedditId = rc.RedditId;"
        )
        NewDB.transaction {
            while (selectReddit.next()) {
                RedditNotifications.insertIgnore {
                    it[RedditNotifications.channelId] = selectReddit.getString("ChannelId")
                    it[RedditNotifications.subreddit] = selectReddit.getString("Instagram")
                }
            }
        }
    }

    fun migrateTwitterAndTwitterChannels() {
        // merged into one table
        val selectTwitter = OldDB.db.connection.createStatement().executeQuery(
            ""
        )
    }

    fun migrateTwitchAndTwitchChannels() {
        // merged into one table
        val selectTwitch = OldDB.db.connection.createStatement().executeQuery(
            ""
        )
    }

    fun migrateAuditingChannels() {
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT ChannelId FROM auditing_channels;"
        )
    }

    // when migrating groups and members, we should take care to change for the new names from web scrape
    // this may take a decent amount of tinkering
    fun migrateGroups() {
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT Guild, TimerLimit FROM guilds;"
        )
    }

    fun migrateMembers() {
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT Guild, TimerLimit FROM guilds;"
        )
    }

    // migrate the tags with their names
    fun migrateTags() {
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT Guild, TimerLimit FROM guilds;"
        )
    }

    fun migrateLinksAndTheirTags() {
        val selectGuilds = OldDB.db.connection.createStatement().executeQuery(
            "SELECT Guild, TimerLimit FROM guilds;"
        )
    }

    migrateUsersAndModerators()
    println("Complete.")

}