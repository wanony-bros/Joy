package com.wanony.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface JoyCommand {
    val commandName: String
    val commandData: CommandData

    suspend fun execute(event: SlashCommandInteractionEvent)
}