package com.wanony.joy.discord.command.autocomplete

import com.wanony.joy.data.dao.DB
import com.wanony.joy.data.dao.Groups
import com.wanony.joy.data.dao.Members
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select

class GroupAutocompleteProvider : com.wanony.joy.discord.command.AutocompleteProvider {

    override val eventName: String = "group"
    override val commandSelector: com.wanony.joy.discord.command.CommandSelector =
        com.wanony.joy.discord.command.ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val member = event.getOption("idol")?.asString
        val query = buildQuery(event, member)
        query.limit(24).map { it[Groups.romanName] }.sorted() + "N/A (No Group)"
    }

    private fun buildQuery(event: CommandAutoCompleteInteractionEvent, member: String?): Query {
        return if (member != null) {
            Groups.innerJoin(Members).select { Members.romanStageName eq member }
        } else {
            Groups.slice(Groups.romanName).select { Groups.romanName like "${event.focusedOption.value}%" }
        }
    }
}


