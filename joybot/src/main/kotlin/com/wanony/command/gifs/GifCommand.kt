package com.wanony.command.gifs

import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.command.gifs.links.LinkProvider.getLink
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class GifCommand : JoyCommand {
    override val commandName: String = "gif"
    override val commandData: CommandData = Commands.slash(
        commandName, "Get a gif from Joy's database!")
        .addOption(OptionType.STRING, "group", "Enter the group", false, true)
        .addOption(OptionType.STRING, "idol", "Enter the idol", false, true)
            // potentially extend to get more than one tag
        .addOption(OptionType.STRING, "tag", "Enter additional tags", false, true)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val groupStr = event.getOption("group")?.asString
        val idol = event.getOption("idol")?.asString
        val tag = event.getOption("tag")?.asString
        val channel = event.channel.idLong

        val link = getLink(channel, groupStr, idol, tag)
        if (link != null) {
            event.reply(link.link).queue()
        } else {
            event.replyEmbeds(Theme.errorEmbed("No link could be found, add links with `/addlink`!").build()).queue()
        }

    }
}