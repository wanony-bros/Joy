package com.wanony.command.instagram

import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler
import com.github.instagram4j.instagram4j.models.media.timeline.ImageCarouselItem
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineCarouselMedia
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineImageMedia
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineMedia
import com.github.instagram4j.instagram4j.models.media.timeline.TimelineVideoMedia
import com.github.instagram4j.instagram4j.models.media.timeline.VideoCarouselItem
import com.github.instagram4j.instagram4j.requests.feed.FeedUserRequest
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest
import com.github.instagram4j.instagram4j.responses.accounts.LoginResponse
import com.github.instagram4j.instagram4j.responses.feed.FeedUserResponse
import com.github.instagram4j.instagram4j.responses.users.UserResponse
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils
import com.wanony.command.JoyCommand
import com.wanony.getProperty
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*


private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"


class InstagramCommand : JoyCommand {
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

    fun handleTimelineVideoMedia(media: TimelineVideoMedia) {
        val videoUrl = media.video_versions[0].url
    }

    fun handleTimelineImageMedia(media: TimelineImageMedia) {
        val imageUrl = media.image_versions2.candidates[0].url
    }

    fun handleTimelineCarousel(media: TimelineCarouselMedia) {
        val mediaUrls = media.carousel_media.map {
            when(it) {
                is VideoCarouselItem -> "boss"// handle this
                is ImageCarouselItem -> "man"// handle this
                else -> "RANDOM FILTER ENABLED."
            }
        }
    }

    suspend fun followInstagramUser(event: SlashCommandInteractionEvent) {
        val username: String = event.getOption("username")!!.asString
        val user: UserResponse = UsersUsernameInfoRequest(username).execute(instagramClient).join()
        val userFeed: FeedUserResponse = FeedUserRequest(user.user.pk).execute(instagramClient).join()
        userFeed.items.forEach {
            when(it) {
                is TimelineVideoMedia -> handleTimelineVideoMedia(it)
                is TimelineImageMedia -> handleTimelineImageMedia(it)
                is TimelineCarouselMedia -> handleTimelineCarousel(it)
                else -> "FUCK THE POLICE."
            }
        }
    }

    suspend fun unfollowInstagramUser(event: SlashCommandInteractionEvent) {}
}