package com.wanony.joy.discord.command

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

typealias CommandSelector = (String) -> Boolean
val ALL_COMMANDS: com.wanony.joy.discord.command.CommandSelector = { _ -> true }

interface AutocompleteProvider {
    val eventName: String
    val commandSelector: com.wanony.joy.discord.command.CommandSelector

    fun autoComplete(event: CommandAutoCompleteInteractionEvent): Boolean {
        if (!commandSelector(eventName)) return false
        if (event.focusedOption.name != eventName) return false
        if (event.focusedOption.type != OptionType.STRING) return false

        event.replyChoiceStrings(
            provideOptions(event).filter { it.startsWith(event.focusedOption.value, true) }
        ).queue()
        return true
    }

    fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String>
}