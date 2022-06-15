package com.wanony.command.instagram

import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler
import com.github.instagram4j.instagram4j.models.media.timeline.ImageCarouselItem
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineCarouselMedia
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineImageMedia
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineVideoMedia
import com.github.instagram4j.instagram4j.models.media.timeline.VideoCarouselItem
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse
import com.github.instagram4j.instagram4j.responses.users.UserResponse
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils
import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.InstagramNotification
import com.wanony.dao.InstagramNotifications
import com.wanony.dao.RedditNotifications
import com.wanony.getProperty
import dev.minn.jda.ktx.generics.getChannel
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*


private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"
private const val INSTAGRAM_COLOUR = 0xDD2A7B


class InstagramCommand(val jda: JDA) : JoyCommand {
    override val commandName: String = "instagram"
    override val commandData: CommandData = Commands.slash(commandName, "Manage Instagram integration")
        .addSubcommands(
            SubcommandData(FOLLOW_OPERATION_NAME, "Follow an Instagram user")
                .addOption(OptionType.STRING, "username", "The user to follow", true),
            SubcommandData(UNFOLLOW_OPERATION_NAME, "Unfollow an Instagram user")
                .addOption(OptionType.STRING, "username", "The user to unfollow", true)
        )

    private var challengeHandler =
        LoginHandler { client: IGClient, response: LoginResponse ->
            IGChallengeUtils.resolveChallenge(client, response) {
                print("Please input code: ")
                Scanner(System.`in`).nextLine()
            }
        }

    private val instagramClient: IGClient = with(IGClient.builder()) {
        username(getProperty("instagramUser"))
        password(getProperty("instagramPassword"))
        onChallenge(challengeHandler)
        login()
    }.also { println("Instagram successfully logged in!") }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            FOLLOW_OPERATION_NAME -> followInstagramUser(event)
            UNFOLLOW_OPERATION_NAME -> unfollowInstagramUser(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun handleTimelineVideoMedia(media: TimelineVideoMedia) {
        val videoUrl = media.video_versions[0].url
    }

    private fun handleTimelineImageMedia(media: TimelineImageMedia) {
        val imageUrl = media.image_versions2.candidates[0].url
    }

    private fun handleTimelineCarousel(media: TimelineCarouselMedia) {
        val mediaUrls = media.carousel_media.map {
            when(it) {
                is VideoCarouselItem -> "boss"// handle this
                is ImageCarouselItem -> "man"// handle this
                else -> "RANDOM FILTER ENABLED."
            }
        }
    }

    private fun checkInstagramForUpdates() = DB.transaction {
        val users: List<ResultRow> = InstagramNotifications.selectAll().toList()
        users.forEach { row ->
            val userId = row[InstagramNotifications.userId].toLong()
            val maxId = row[InstagramNotifications.lastSent]
            val userFeed: FeedUserResponse = FeedUserRequest(userId, maxId).execute(instagramClient).join()
            if (userFeed.items.size == 0) return@forEach
            val channels =
                InstagramNotifications.slice(InstagramNotifications.channelId).select {
                    InstagramNotifications.userId eq userId.toString()
                }.map {
                    it[InstagramNotifications.channelId].toLong()
                }
            userFeed.items.forEach {
                val processedItem = when(it) {
                    // TODO implement these
                    is TimelineVideoMedia -> handleTimelineVideoMedia(it)
                    is TimelineImageMedia -> handleTimelineImageMedia(it)
                    is TimelineCarouselMedia -> handleTimelineCarousel(it)
                    else -> "FUCK THE POLICE."
                }
                channels.mapNotNull { c -> jda.getChannel<MessageChannel>(c) }.forEach { channel ->
                    channel.sendMessageEmbeds(processedItem)
                }
            }

            InstagramNotifications.update({ InstagramNotifications.userId eq userId.toString() }) {
                it[lastSent] =  userFeed.items[0].id
            }

        }
    }

    private fun followInstagramUser(event: SlashCommandInteractionEvent) {
        val username: String = event.getOption("username")!!.asString
        val user: UserResponse = UsersUsernameInfoRequest(username).execute(instagramClient).join()
        if (user.user.is_private) {
            // we are unable to access posts from private users, so we error and return
            event.replyEmbeds(Theme.errorEmbed("Unable to follow private users!").build()).queue()
            return
        }
        val userFeed: FeedUserResponse = FeedUserRequest(user.user.pk).execute(instagramClient).join()
        val latestPostId = userFeed.items.firstOrNull()?.id // get the latest post id
        val added: Boolean = DB.transaction {
            InstagramNotifications.insert {
                it[userId] = user.user.pk.toString()
                it[channelId] = event.channel.id
                it[lastSent] = latestPostId
            }.insertedCount > 0
        }
        if (!added) {
            // this case is most likely because it's already followed
            event.replyEmbeds(Theme.errorEmbed("Failed to follow $username\nUser is likely already followed in this channel.").build()).setEphemeral(true).queue()
            return
        }
        event.replyEmbeds(EmbedBuilder().apply {
            setTitle("Successfully followed ${MarkdownSanitizer.escape(user.user.full_name)}")
            setThumbnail(user.user.profile_pic_url)
            setDescription("Instagram updates from ${MarkdownSanitizer.escape(username)} will be posted in this channel!")
            setColor(INSTAGRAM_COLOUR)
        }.build()).queue()
    }

    private fun unfollowInstagramUser(event: SlashCommandInteractionEvent) {
        val username: String = event.getOption("username")!!.asString
        val user = UsersUsernameInfoRequest(username).execute(instagramClient).join()
        val deleted = DB.transaction {
            InstagramNotifications.deleteWhere {
                InstagramNotifications.userId eq user.user.pk.toString() and (InstagramNotifications.channelId eq event.channel.id)
            }
        }
        if (deleted == 0) {
            // we didn't find it, so we error out
            event.replyEmbeds(
                Theme.errorEmbed("${MarkdownSanitizer.escape(username)} not followed in ${event.channel.name}!").build()
            ).setEphemeral(true).queue()
            return
        }
        event.replyEmbeds(
            Theme.successEmbed(
                "Unfollowed ${MarkdownSanitizer.escape(user.user.full_name)} in ${event.channel.name}").build()
        ).setEphemeral(true).queue()
    }
}