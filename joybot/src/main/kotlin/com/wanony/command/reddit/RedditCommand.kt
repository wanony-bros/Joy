package com.wanony.command.reddit

import com.wanony.command.JoyCommand
import com.wanony.reddit.api.RedditClient
import com.wanony.reddit.impl.DefaultRedditClient
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

private const val FOLLOW_OPERATION_NAME = "follow"
private const val UNFOLLOW_OPERATION_NAME = "unfollow"

class RedditCommand : JoyCommand {
    lateinit var redditClient: RedditClient
    override val commandName: String = "reddit"
    override val commandData: CommandData = Commands.slash(commandName, "Follow or unfollow a subreddit")
        .addSubcommands(
            SubcommandData(FOLLOW_OPERATION_NAME, "Follow a subreddit")
                .addOption(OptionType.STRING, "subreddit", "The subreddit to follow", true),
            SubcommandData(UNFOLLOW_OPERATION_NAME, "Unfollow a subreddit")
                .addOption(OptionType.STRING, "subreddit", "The subreddit to unfollow", true)
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            FOLLOW_OPERATION_NAME -> followSubreddit(event)
            UNFOLLOW_OPERATION_NAME -> unfollowSubreddit(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun unfollowSubreddit(event: SlashCommandInteractionEvent) {
        val subreddit = event.getOption("subreddit")!!.asString
    }

    private fun followSubreddit(event: SlashCommandInteractionEvent) {
        val subreddit = event.getOption("subreddit")!!.asString
    }
}