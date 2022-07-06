package com.wanony.command.twitter

import com.google.common.reflect.TypeToken
import com.twitter.clientlib.ApiException
import com.twitter.clientlib.JSON
import com.twitter.clientlib.TwitterCredentialsBearer
import com.twitter.clientlib.api.TwitterApi
import com.twitter.clientlib.model.AddOrDeleteRulesRequest
import com.twitter.clientlib.model.AddRulesRequest
import com.twitter.clientlib.model.FilteredStreamingTweet
import com.twitter.clientlib.model.RuleNoId
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.findProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

private const val TWITTER_FOLLOW_OPERATION_NAME = "follow"
private const val TWITTER_UNFOLLOW_OPERATION_NAME = "unfollow"

private val TWITTER_EXPANSIONS = setOf("author_id") // Don't need yet
private val TWITTER_TWEET_FIELDS = setOf("created_at")

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
                    val obj = JSON.getGson().fromJson<FilteredStreamingTweet>(line, localVarReturnType)
                    println(obj)
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

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            TWITTER_FOLLOW_OPERATION_NAME -> followTwitterUser(event)
            TWITTER_UNFOLLOW_OPERATION_NAME -> unfollowTwitterUser(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private suspend fun followTwitterUser(event: SlashCommandInteractionEvent) {
        val user = event.getOption("username")?.asString
        val twitterUser = twitter.users().findUserByUsername(user, null, null, null)
        print(twitterUser.data)
        val aOrDRequest = AddRulesRequest().addAddItem(RuleNoId().value("from:${user}"))
        twitter.tweets().addOrDeleteRules(AddOrDeleteRulesRequest(aOrDRequest), false)
    }

    private fun unfollowTwitterUser(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }
}