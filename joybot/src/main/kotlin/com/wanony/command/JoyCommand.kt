package com.wanony.command

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface JoyCommand {
    val commandName: String
    val commandData: CommandData

    suspend fun execute(event: SlashCommandInteractionEvent)

    /**
     * Sets up the command, could be connecting to apis, reading credentials or anything else.
     *
     * This method should return true if the command was set up correctly or false otherwise.
     * On returning false this command will not be registered.
     */
    fun setup() : Boolean = true
}
