package com.wanony.joy.discord.command.reddit

import com.wanony.joy.data.DB
import com.wanony.joy.discord.Theme
import com.wanony.joy.discord.Theme.Companion.toLink
import com.wanony.joy.discord.checkGuildReplyPermissions
import com.wanony.joy.data.dao.RedditNotifications
import com.wanony.joy.discord.command.JoyCommand
import com.wanony.joy.discord.findProperty
import com.wanony.reddit.api.RedditClient
import com.wanony.reddit.api.json.Listing
import com.wanony.reddit.impl.DefaultRedditClient
import dev.minn.jda.ktx.generics.getChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.*
import java.util.concurrent.TimeUnit

private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"
private const val CLEAR_OPERATION_NAME = "clear"

class RedditCommand(val jda: JDA) : JoyCommand {
    private lateinit var redditClient: RedditClient
    override val commandName: String = "reddit"
    override val commandData: CommandData = Commands.slash(commandName, "Follow or unfollow a subreddit")
        .addSubcommands(
            SubcommandData(FOLLOW_OPERATION_NAME, "Follow a subreddit")
                .addOption(OptionType.STRING, "subreddit", "The subreddit to follow", true),
            SubcommandData(UNFOLLOW_OPERATION_NAME, "Unfollow a subreddit")
                .addOption(OptionType.STRING, "subreddit", "The subreddit to unfollow", true),
            SubcommandData(CLEAR_OPERATION_NAME, "Remove all followed subreddits from this channel")
        )

    override fun setup() : Boolean {
        val token = findProperty<String>("redditToken") ?: return false
        val secret = findProperty<String>("redditSecret") ?: return false
        redditClient = DefaultRedditClient(token, secret)

        CoroutineScope(Dispatchers.Default).launch {
            checkRedditForUpdates(false) // update most recent posts, so we don't get spammed on restart
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(5))
                checkRedditForUpdates(true)
            }
        }

        return true
    }

    private fun checkRedditForUpdates(post: Boolean) = DB.transaction {
        val subreddits: List<ResultRow> = RedditNotifications.selectAll().toList()
        subreddits.forEach { row ->
            val sub = row[RedditNotifications.subreddit]
            val lastSent = row[RedditNotifications.lastSent]
            val listings: Listing = redditClient.subreddit(sub, lastSent) ?: return@forEach
            val mostRecentlySent = listings.links.firstOrNull()?.name() ?: return@forEach
            if (post) {
                val channels =
                    RedditNotifications.slice(RedditNotifications.channelId).select {
                        RedditNotifications.subreddit eq sub
                    }.map {
                        it[RedditNotifications.channelId].toLong()
                    }
                channels.mapNotNull { c -> jda.getChannel<MessageChannel>(c) }.forEach { channel ->
                    listings.links.forEach {
                        channel.sendMessageEmbeds(EmbedBuilder().apply {
                            setTitle(it.title()?.take(256))
                            setDescription(
                                """
                                   Posted by ${it.author()} in **/r/${it.subreddit()}**
                                   **Post Permalink**:
                                   https://www.reddit.com${it.permalink()}
                                   ${if (it.url()?.trim()?.endsWith(it.permalink().trim()) == true) "" else "**${it.url()}**"}
                                """.trimIndent()
                            )
                        }.build()).queue()
                        it.url()?.let { url ->
                            if (url.startsWith("https://gfycat.com/")) {
                                channel.sendMessage(url).queue()
                            } else if (url.startsWith("https://i.imgur.com/")) {
                                // add check for imgur, now that gfycat has some upload issues
                                channel.sendMessage(url).queue()
                            }
                        }
                    }
                }
            }
            RedditNotifications.update({ RedditNotifications.subreddit eq sub }) {
                it[RedditNotifications.lastSent] = mostRecentlySent
            }
        }
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            FOLLOW_OPERATION_NAME -> followSubreddit(event)
            UNFOLLOW_OPERATION_NAME -> unfollowSubreddit(event)
            CLEAR_OPERATION_NAME -> clearAll(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun clearAll(event: SlashCommandInteractionEvent) {
        val channel = event.channel as? GuildMessageChannel ?: return replyGuildRequiredError(event)

        val removed = DB.transaction {
            RedditNotifications.deleteWhere {
                RedditNotifications.channelId eq channel.id
            }
        }

        val message = if (removed == 0) {
            "There were no subreddits followed in ${channel.toLink()}."
        } else {
            "Stopped following $removed subreddits."
        }

        event.replyEmbeds(Theme.successEmbed(message).build()).queue()
    }

    private fun followSubreddit(event: SlashCommandInteractionEvent) {
        val subreddit = event.getOption("subreddit")!!.asString
        val channel = event.channel as? GuildMessageChannel ?: return replyGuildRequiredError(event)

        if (event.checkGuildReplyPermissions()) {
            event.replyEmbeds(
                Theme.errorEmbed(
                    """Joy does not have permission to send message in ${channel.name}
                        Please update the permissions and try again!
                    """.trimIndent()).build()).setEphemeral(true).queue()
            return
        }

        val listing: Listing = redditClient.subreddit(subreddit) ?: return replySubredditNotFoundError(event, subreddit)
        val newestId = listing.links[0].name()
        val inserted = DB.transaction {
            RedditNotifications.insertIgnore {
                it[RedditNotifications.subreddit] = subreddit
                it[channelId] = channel.id
                it[lastSent] = newestId
            }.insertedCount > 0
        }
        if (!inserted) {
            event.replyEmbeds(Theme.errorEmbed("Failed to follow $subreddit!\nPlease check if it is already followed in this channel.").build()).queue()
            return
        }

        event.replyEmbeds(Theme.successEmbed("Updates from $subreddit will be received in this channel!").build()).queue()
    }

    private fun unfollowSubreddit(event: SlashCommandInteractionEvent) {
        val subreddit = event.getOption("subreddit")!!.asString
        val channel = event.channel
        val deleted = DB.transaction {
            RedditNotifications.deleteWhere {
                RedditNotifications.subreddit eq subreddit and (RedditNotifications.channelId eq channel.id)
            }
        }
        if (deleted == 0) {
            event.replyEmbeds(
                Theme.errorEmbed("$subreddit not followed in ${channel.name}!").build()).setEphemeral(true).queue()
            return
        }
        event.replyEmbeds(Theme.successEmbed("Unfollowed $subreddit in ${channel.name}").build()).setEphemeral(true).queue()
    }

    private fun replySubredditNotFoundError(event: SlashCommandInteractionEvent, subreddit: String) {
        event.replyEmbeds(Theme.errorEmbed("No subreddit found called $subreddit").build()).setEphemeral(true).queue()
    }

    private fun replyGuildRequiredError(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(Theme.errorEmbed("This command can only be used within a server.").build()).setEphemeral(true).queue()
    }
}