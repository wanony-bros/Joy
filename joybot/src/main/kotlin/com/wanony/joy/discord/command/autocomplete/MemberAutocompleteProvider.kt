package com.wanony.joy.discord.command.autocomplete

import com.wanony.joy.data.dao.DB
import com.wanony.joy.data.dao.Groups
import com.wanony.joy.data.dao.Members
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class MemberAutocompleteProvider : com.wanony.joy.discord.command.AutocompleteProvider {
    override val eventName: String = "idol"
    override val commandSelector: com.wanony.joy.discord.command.CommandSelector =
        com.wanony.joy.discord.command.ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val group = event.getOption("group")?.asString
        val query = buildMemberQuery(event, group)
        query.limit(25).map { it[Members.romanStageName] }.sorted()
    }

    private fun buildMemberQuery(event: CommandAutoCompleteInteractionEvent, group: String?): Query {
        return if (group != null && group != "N/A (No Group)") {
            Members.innerJoin(Groups).select { Groups.romanName eq group }
        } else if (group == "N/A (No Group)") {
            Members.slice(Members.romanStageName).select { Members.groupId.isNull() and (Members.romanStageName like "${event.focusedOption.value}%") }
        } else {
            Members.slice(Members.romanStageName).select { Members.romanStageName like "${event.focusedOption.value}%" }
        }
    }

    fun cleanName(name: String) {

    }
}