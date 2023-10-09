package com.wanony.joy.discord.command.gifs

import com.wanony.joy.discord.Theme
import com.wanony.joy.discord.command.JoyCommand
import com.wanony.joy.discord.command.gifs.links.LinkProvider
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class RandomLinkCommand : JoyCommand {
    override val commandName: String = "random"
    override val commandData: CommandData =
        Commands.slash(commandName, "Get a random link from Joy's database!")

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val channel = event.channel.idLong
        val link = LinkProvider.getLink(channel)
        if (link != null) {
            event.reply("Random choice! `${link.group}`'s `${link.member}`!\n${link.link}").queue()
        } else {
            event.replyEmbeds(Theme.errorEmbed("No link could be found, add links with `/addlink`!").build()).queue()
        }
    }
}