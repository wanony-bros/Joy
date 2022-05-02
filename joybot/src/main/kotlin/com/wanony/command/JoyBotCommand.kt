package com.wanony.command

import com.wanony.command.misc.AvatarCommand
import com.wanony.command.misc.SuggestCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

val allCommands : Map<String, JoyBotCommand> = listOf(
    AvatarCommand(),
    SuggestCommand(),
).associateBy { it.name }

interface JoyBotCommand {
    val name: String
    val commandData: CommandData

    fun execute(event: SlashCommandInteractionEvent)
}