package com.wanony.command.twitter

import com.google.common.reflect.TypeToken
import com.twitter.clientlib.ApiException
import com.twitter.clientlib.JSON
import com.twitter.clientlib.TwitterCredentialsBearer
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.AddOrDeleteRulesRequest
import com.twitter.clientlib.model.AddRulesRequest
import com.twitter.clientlib.model.DeleteRulesRequest
import com.twitter.clientlib.model.FilteredStreamingTweet
import com.twitter.clientlib.model.RuleNoId
import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.TwitterNotifications
import com.wanony.findProperty
import dev.minn.jda.ktx.messages.EmbedBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.io.BufferedReader
import java.io.InputStreamReader

private const val TWITTER_COLOUR = 0x1DA1F2

private const val TWITTER_FOLLOW_OPERATION_NAME = "follow"
private const val TWITTER_UNFOLLOW_OPERATION_NAME = "unfollow"

private val TWITTER_EXPANSIONS = setOf("author_id") // Don't need yet
private val TWITTER_TWEET_FIELDS = setOf("created_at")

private val TWITTER_USER_FIELDS = setOf("protected", "profile_image_url")

class TwitterCommand : JoyCommand {
    private lateinit var twitter: TwitterApi
    override val commandName: String = "twitter"
    override val commandData: CommandData = Commands.slash(commandName, "Follow or unfollow a twitter user")
        .addSubcommands(
            SubcommandData(TWITTER_FOLLOW_OPERATION_NAME, "Follow a twitter user")
                .addOption(OptionType.STRING, "username", "The twitter user to follow", true),
            SubcommandData(TWITTER_UNFOLLOW_OPERATION_NAME, "Unfollow a twitter user")
                .addOption(OptionType.STRING, "username", "The twitter user to unfollow", true)
        )

    override fun setup(): Boolean {
        val twitterBearer = findProperty<String>("twitterBearerToken") ?: return false
        twitter = TwitterApi()
        twitter.setTwitterCredentials(TwitterCredentialsBearer(twitterBearer))
        CoroutineScope(Dispatchers.Default).launch {
            checkTwitterForUpdates()
        }

        return true
    }

    private fun checkTwitterForUpdates() {
        try {
            val aOrDRequest = AddRulesRequest().addAddItem(RuleNoId().value("from:PURGETHEUNHOLY"))
            twitter.tweets().addOrDeleteRules(AddOrDeleteRulesRequest(aOrDRequest), false)
            val stream = twitter.tweets().searchStream(TWITTER_EXPANSIONS, TWITTER_TWEET_FIELDS, null, null, null, null, null)
            try {
                val localVarReturnType = (object : TypeToken<FilteredStreamingTweet>(){}).type
                val reader = BufferedReader(InputStreamReader(stream))
                var line = reader.readLine()
                while (line != null) {
                    if (line.isEmpty()) {
                        line = reader.readLine()
                        continue
                    }
                    val tweet = JSON.getGson().fromJson<FilteredStreamingTweet>(line, localVarReturnType)
                    println(tweet)
                    postNewTweet(tweet)
                    line = reader.readLine()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println(e)
            }
        } catch (e: ApiException) {
            System.err.println("Status code: " + e.code);
            System.err.println("Reason: " + e.responseBody);
            System.err.println("Response headers: " + e.responseHeaders);
            e.printStackTrace();
        }
    }

    private fun postNewTweet(tweet: FilteredStreamingTweet) {
        TODO("Get all channels this needs to post to, and format tweet in a way that looks nice.")
        TODO("If no channels are following this user, remove the rule that gets updates from this twitter user")
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            TWITTER_FOLLOW_OPERATION_NAME -> followTwitterUser(event)
            TWITTER_UNFOLLOW_OPERATION_NAME -> unfollowTwitterUser(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun followTwitterUser(event: SlashCommandInteractionEvent) {
        val user = event.getOption("username")?.asString
        val twitterUser = twitter.users().findUserByUsername(user, null, null, TWITTER_USER_FIELDS)
        print(twitterUser.data)
        if (twitterUser.data?.protected != false) {
            event.replyEmbeds(
                Theme.errorEmbed("Unable to follow user ${MarkdownSanitizer.escape(user!!)} as their tweets are protected!").build()).queue()
            return
        }
        val added: Boolean = DB.transaction { TwitterNotifications.insert {
            it[twitterId] = twitterUser.data?.id.toString()
            it[channelId] = event.channel.id
        } }.insertedCount > 0
        if (!added) {
            event.replyEmbeds(Theme.errorEmbed("Failed to follow ${MarkdownSanitizer.escape(user!!)}!\nUser is likely already followed in this channel").build()).queue()
            return
        }
        val aOrDRequest = AddRulesRequest().addAddItem(RuleNoId().value("from:${user}"))
        twitter.tweets().addOrDeleteRules(AddOrDeleteRulesRequest(aOrDRequest), false)
        val thumb = twitterUser.data?.profileImageUrl.toString().dropLast(11) + ".jpg"
        event.replyEmbeds(
            EmbedBuilder().apply {
                this.title = MarkdownSanitizer.escape("Successfully followed ${twitterUser.data?.name} in this channel!")
                this.description = "From now on Tweets made by [@${twitterUser.data?.username}](https://www.twitter.com/${twitterUser.data?.username}) will be posted in this channel."
                this.thumbnail = thumb
                this.color = TWITTER_COLOUR
            }.build()
        ).queue()
    }

    private fun unfollowTwitterUser(event: SlashCommandInteractionEvent) {
        val user = event.getOption("username")?.asString
        // No need to remove the rule, as we just remove it posting to that channel
        val twitterUser = twitter.users().findUserByUsername(user, null, null, TWITTER_USER_FIELDS)
        val deleted = DB.transaction { TwitterNotifications.deleteWhere {
                TwitterNotifications.twitterId eq twitterUser.data?.id.toString() and (TwitterNotifications.channelId eq event.channel.id)
            }
        }
        if (deleted == 0) {
            event.replyEmbeds(
                Theme.errorEmbed("${MarkdownSanitizer.escape(user!!)} not followed in this channel!").build()
            ).setEphemeral(true).queue()
            return
        }
        event.replyEmbeds(
            Theme.successEmbed("Successfully unfollowed $user in this channel!").build()
        ).queue()
    }
}