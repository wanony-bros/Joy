package com.wanony.joy.discord.command.autocomplete

import com.wanony.joy.discord.command.AutocompleteProvider
import com.wanony.joy.discord.command.CommandSelector
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import kotlinx.coroutines.Job

class TimerAutocompleteProvider() : com.wanony.joy.discord.command.AutocompleteProvider {
    override val eventName: String = "id"
    override val commandSelector: com.wanony.joy.discord.command.CommandSelector = { it == "stop" }

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> {
        val userId = event.user.id
        TODO("Need to find a way to get the timers in this class")
        // return timers.keys.filter { it.startsWith(userId) }
    }
}