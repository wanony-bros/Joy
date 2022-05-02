package com.wanony.command.misc

import com.wanony.command.JoyBotCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

class AvatarCommand : JoyBotCommand {
    override val name: String = "avatar"
    override val commandData: CommandData =
        Commands.slash(name, "See a larger version of a user avatar")
            .addOption(OptionType.USER, "user", "Select a user")

    override fun execute(event: SlashCommandInteractionEvent) {
        val member = event.getOption("user")?.asMember ?: event.member
        val user = event.getOption("user")?.asUser ?: event.user
        val embed = EmbedBuilder().apply {
            setColor(member?.color)
            setAuthor(member?.effectiveName ?: user.name)
            setFooter("requested by ${event.user.asTag}", event.user.effectiveAvatarUrl)
            // TODO: Use Member#getEffectiveAvatar to use a bigger image
            setImage(member?.effectiveAvatarUrl ?: user.effectiveAvatarUrl)
        }.build()
        event.replyEmbeds(embed).queue()
    }
}