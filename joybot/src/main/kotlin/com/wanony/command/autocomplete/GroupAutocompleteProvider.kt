package com.wanony.command.autocomplete

import com.wanony.DB
import com.wanony.command.ALL_COMMANDS
import com.wanony.command.AutocompleteProvider
import com.wanony.command.CommandSelector
import com.wanony.dao.Groups
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.selectAll

class GroupAutocompleteProvider : AutocompleteProvider {

    override val eventName: String = "group"
    override val commandSelector: CommandSelector = ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        Groups.slice(Groups.romanName).selectAll().limit(25).map {
            it[Groups.romanName]
        }.sorted()
    }
}

