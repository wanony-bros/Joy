package com.wanony.command.reddit

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.RedditNotifications
import com.wanony.getProperty
import com.wanony.reddit.api.json.Listing
import com.wanony.reddit.impl.DefaultRedditClient
import dev.minn.jda.ktx.generics.getChannel
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.internal.utils.PermissionUtil
import org.jetbrains.exposed.sql.*

private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"

class RedditCommand(val jda: JDA) : JoyCommand {
//    lateinit var redditClient: RedditClient
    private val redditClient = DefaultRedditClient(getProperty("redditToken"), getProperty("redditSecret"))
    init {
        checkRedditForUpdates()
    }
    override val commandName: String = "reddit"
    override val commandData: CommandData = Commands.slash(commandName, "Follow or unfollow a subreddit")
        .addSubcommands(
            SubcommandData(FOLLOW_OPERATION_NAME, "Follow a subreddit")
                .addOption(OptionType.STRING, "subreddit", "The subreddit to follow", true)
                .addOption(OptionType.CHANNEL, "channel", "The channel to receive updates", true),
            SubcommandData(UNFOLLOW_OPERATION_NAME, "Unfollow a subreddit")
                .addOption(OptionType.CHANNEL, "channel", "The channel to stop updates from being posted", true)
                .addOption(OptionType.STRING, "subreddit", "The subreddit to unfollow", true)
        )

    private fun checkRedditForUpdates() = DB.transaction {
        val subreddits: List<ResultRow> = RedditNotifications.selectAll().toList()
        subreddits.forEach { row ->
            val sub = row[RedditNotifications.subreddit]
            val lastSent = row[RedditNotifications.lastSent]
            val listings: Listing = redditClient.subreddit(sub, lastSent) ?: return@forEach
            val mostRecentlySent = listings.links.firstOrNull()?.name() ?: return@forEach
            val channels =
                RedditNotifications.slice(RedditNotifications.channelId).select {
                    RedditNotifications.subreddit eq sub
                }.map {
                    it[RedditNotifications.channelId].toLong()
                }
            channels.mapNotNull { c -> jda.getChannel<MessageChannel>(c) }.forEach { channel ->
                    listings.links.forEach {
                        // TODO check for permissions to send message here
                        channel.sendMessageEmbeds(EmbedBuilder().apply {
                            setTitle(it.title()?.take(256))
                            setDescription(
                                """Posted by ${it.author()} in **/r/${it.subreddit()}**
                                   **Post Permalink**:
                                   https://reddit.com${it.permalink()}
                                   **${it.url()}**
                                """.trimIndent()
                            )
                        }.build()).queue()
                        it.url()?.let { url ->
                            if (url.startsWith("https://gfycat.com/")) {
                                channel.sendMessage(url).queue()
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
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun followSubreddit(event: SlashCommandInteractionEvent) {
        println(event.name)
        val subreddit = event.getOption("subreddit")!!.asString
        val channel: GuildMessageChannel = event.getOption("channel")!!.asMessageChannel ?: return
        val listing: Listing = redditClient.subreddit(subreddit) ?: return subredditNotFoundError(event, subreddit)
        val newestId = listing.links[0].name()
        val inserted = DB.transaction {
            RedditNotifications.insert {
                it[RedditNotifications.subreddit] = subreddit
                it[channelId] = channel.id
                it[lastSent] = newestId
            }.insertedCount > 0
        }
        if (!inserted) {
            event.replyEmbeds(Theme.errorEmbed("Failed to follow $subreddit!\nPlease check if it is already followed in this channel.").build()).queue()
            return
        }
        // TODO convert to a function, probably
        // TODO if fails, send a message to the author saying no access to channel
        val joy = event.guild?.getMember(event.jda.selfUser)
        if (!PermissionUtil.checkPermission(channel.permissionContainer, joy, Permission.MESSAGE_SEND)) {
            event.replyEmbeds(
                Theme.errorEmbed(
                    """Joy does not have permission to send message in ${channel.name}
                        Please update the permissions and try again!
                    """.trimIndent()).build()).setEphemeral(true).queue()
            return
        }

        channel.sendMessageEmbeds(Theme.successEmbed("Updates from $subreddit will be received in this channel!").build()).queue()
        event.replyEmbeds(Theme.successEmbed("Followed $subreddit in ${channel.name}").build()).setEphemeral(true).queue()
    }

    private fun unfollowSubreddit(event: SlashCommandInteractionEvent) {
        val subreddit = event.getOption("subreddit")!!.asString
        val channel: GuildMessageChannel = event.getOption("channel")!!.asMessageChannel ?: return
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
        channel.sendMessageEmbeds(Theme.successEmbed("$subreddit unfollowed in this channel!").build()).queue()
        event.replyEmbeds(Theme.successEmbed("Unfollowed $subreddit in ${channel.name}").build()).setEphemeral(true).queue()
    }

    private fun subredditNotFoundError(event: SlashCommandInteractionEvent, subreddit: String) {
        event.replyEmbeds(Theme.errorEmbed("No subreddit found called $subreddit").build()).queue()
    }


}