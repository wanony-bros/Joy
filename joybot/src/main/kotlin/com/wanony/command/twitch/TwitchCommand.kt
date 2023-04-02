package com.wanony.command.twitch

import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.helix.domain.User
import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.InstagramNotifications
import com.wanony.dao.TwitchNotifications
import com.wanony.getProperty
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"
private const val TWITCH_COLOUR = 0x6441a5

class TwitchCommand : JoyCommand {
    override val commandName: String = "twitch"
    override val commandData: CommandData = Commands.slash(commandName, "Manage Twitch integration")
        .addSubcommands(
            SubcommandData(FOLLOW_OPERATION_NAME, "Follow an Twitch user")
                .addOption(OptionType.STRING, "username", "The user to follow", true),
            SubcommandData(UNFOLLOW_OPERATION_NAME, "Unfollow an Instagram user")
                .addOption(OptionType.STRING, "username", "The user to unfollow", true)
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            FOLLOW_OPERATION_NAME -> followTwitchUser(event)
            UNFOLLOW_OPERATION_NAME -> unfollowTwitchUser(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private val twitchClient = TwitchClientBuilder.builder()
        .withClientId(getProperty("twitchClientId"))
        .withClientSecret(getProperty("twitchClientSecret"))
        .withEnableHelix(true)
        .build()

    private fun twitchUserEmbed(user: User,
                                permalink: String,
    ): EmbedBuilder = EmbedBuilder().apply {
        setTitle("Successfully followed ${MarkdownSanitizer.escape(user.displayName)}")
        setDescription("When a livestream begins, this channel will receive an update!\nhttps://www.instagram.com/p/${permalink}/")
        setThumbnail(user.profileImageUrl)
        setColor(TWITCH_COLOUR)
    }

    private suspend fun followTwitchUser(event: SlashCommandInteractionEvent) {
        val username: String = event.getOption("username")!!.asString
        val resultList = twitchClient.helix.getUsers(null, null, listOf(username)).execute()
        val foundUser = resultList.users.firstOrNull { user -> user.login == username }
        if (foundUser == null) {
            // we didn't find the username, return error
            event.replyEmbeds(Theme.errorEmbed("No user with username: $username found!").build()).queue()
            return
        }
        val added: Boolean = DB.transaction {
            TwitchNotifications.insert {
                it[userId] = foundUser.id
                it[channelId] = event.channel.id
            }.insertedCount > 0
        }
        if (added) {
            val embed: EmbedBuilder = EmbedBuilder().apply {
                setTitle("Successfully followed ${MarkdownSanitizer.escape(foundUser.displayName)}")
                setDescription("When a livestream begins, this channel will receive an update!\nhttps://www.twitch.tv/${foundUser.login}/")
                setThumbnail(foundUser.profileImageUrl)
                setColor(TWITCH_COLOUR)
            }
            event.replyEmbeds(embed.build()).queue()
        } else {
            event.replyEmbeds(Theme.errorEmbed(
                    "Failed to follow Twitch user $username!\n" +
                            "Check if this user is already followed in this channel").build()).queue()
        }
    }

    private suspend fun unfollowTwitchUser(event: SlashCommandInteractionEvent) {
        val username: String = event.getOption("username")!!.asString
        val resultList = twitchClient.helix.getUsers(null, null, listOf(username)).execute()
        val foundUser = resultList.users.firstOrNull { user -> user.login == username }
        if (foundUser == null) {
            // we didn't find the username, return error
            event.replyEmbeds(Theme.errorEmbed("No user with username: $username found!").build()).queue()
            return
        }
        val deleted = DB.transaction {
            TwitchNotifications.deleteWhere {
                TwitchNotifications.userId eq foundUser.id and (TwitchNotifications.channelId eq event.channel.id)
            }
        }
        if (deleted == 0) {
            // if we didn't delete anything
            event.replyEmbeds(Theme.errorEmbed(
                "Failed to unfollow Twitch user $username!\n" +
                        "Please check that this user is followed in this channel!").build()).queue()
        } else {
            val embed: EmbedBuilder = EmbedBuilder().apply {
                setTitle("Successfully unfollowed ${MarkdownSanitizer.escape(foundUser.displayName)}")
                setDescription("Updates will no longer be received from: \nhttps://www.twitch.tv/${foundUser.login}/")
                setThumbnail(foundUser.profileImageUrl)
                setColor(TWITCH_COLOUR)
            }
            event.replyEmbeds(embed.build()).queue()
        }
    }
}