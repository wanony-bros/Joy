package com.wanony.command.autocomplete

import com.wanony.DB
import com.wanony.command.ALL_COMMANDS
import com.wanony.command.AutocompleteProvider
import com.wanony.command.CommandSelector
import com.wanony.dao.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class TagAutocompleteProvider : AutocompleteProvider {
    override val eventName: String = "tag"
    override val commandSelector: CommandSelector = ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val group = event.getOption("group")?.asString
        val member = event.getOption("idol")?.asString
        val query = buildTagQuery(group, member)
        query.limit(25).map { it[Tags.tagName] }.sorted()
    }

    private fun buildTagQuery(group: String?, member: String?): Query {
        return if (group != null && member != null) {
            Tags.innerJoin(LinkTags).innerJoin(Links).innerJoin(LinkMembers).innerJoin(Members).innerJoin(Groups)
                .select { Groups.romanName eq group and (Members.romanStageName eq member) }.groupBy(Tags.tagName)
        } else if (group != null) {
            Tags.innerJoin(LinkTags).innerJoin(Links).innerJoin(LinkMembers).innerJoin(Members).innerJoin(Groups)
                .select { Groups.romanName eq group }.groupBy(Tags.tagName)
        } else {
            Tags.slice(Tags.tagName).selectAll().groupBy(Tags.tagName)
        }
    }
}
