package com.wanony.command.autocomplete

import com.wanony.DB
import com.wanony.command.ALL_COMMANDS
import com.wanony.command.AutocompleteProvider
import com.wanony.command.CommandSelector
import com.wanony.dao.Groups
import com.wanony.dao.Members
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.select

class GroupAutocompleteProvider : AutocompleteProvider {

    override val eventName: String = "group"
    override val commandSelector: CommandSelector = ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val member = event.getOption("idol")?.asString
        val query = if (member != null) {
            Groups.innerJoin(Members).select { Members.romanStageName eq member }
        } else {
            Groups.slice(Groups.romanName).select { Groups.romanName like "${event.focusedOption.value}%" }
        }
        query.limit(25).map {
            it[Groups.romanName]
        }.sorted()
    }
}

