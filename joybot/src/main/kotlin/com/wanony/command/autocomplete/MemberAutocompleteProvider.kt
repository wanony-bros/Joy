package com.wanony.command.autocomplete

import com.wanony.DB
import com.wanony.command.ALL_COMMANDS
import com.wanony.command.AutocompleteProvider
import com.wanony.command.CommandSelector
import com.wanony.dao.Groups
import com.wanony.dao.Members
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.select

class MemberAutocompleteProvider : AutocompleteProvider {
    override val eventName: String = "idol"
    override val commandSelector: CommandSelector = ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val group = event.getOption("group")?.asString
        val query = if (group != null) {
            Members.innerJoin(Groups).select { Groups.romanName eq group }
        } else {
            Members.slice(Members.romanStageName).select { Members.romanStageName like "${event.focusedOption.value}%" }
        }
        query.limit(25).map {
            it[Members.romanStageName]
        }.sorted()
    }

    fun cleanName(name: String) {

    }
}