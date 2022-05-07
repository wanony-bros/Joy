package com.wanony.command

import com.wanony.command.gfys.AddLinkCommand
import com.wanony.command.gfys.RandomLinkCommand
import com.wanony.command.misc.AvatarCommand
import com.wanony.command.misc.SuggestCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

val allCommands : Map<String, JoyCommand> = listOf(
    AvatarCommand(),
    SuggestCommand(),
    AddLinkCommand(),
    RandomLinkCommand(),
).associateBy { it.name }

interface JoyCommand {
    val name: String
    val commandData: CommandData

    fun execute(event: SlashCommandInteractionEvent)
}