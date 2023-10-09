package com.wanony.joy.discord.command

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

interface CustomButton {
    val buttonId: String

    suspend fun execute(event: ButtonInteractionEvent)

    /**
     * Sets up the command, could be connecting to apis, reading credentials or anything else.
     *
     * This method should return true if the command was set up correctly or false otherwise.
     * On returning false this command will not be registered.
     */
    fun setup() : Boolean = true
}