package com.wanony.command.autocomplete

import com.wanony.DB
import com.wanony.command.ALL_COMMANDS
import com.wanony.command.AutocompleteProvider
import com.wanony.command.CommandSelector
import com.wanony.dao.Memes
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select

class MemeAutocompleteProvider : AutocompleteProvider {

    override val eventName: String = "meme"
    override val commandSelector: CommandSelector = ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val query = buildQuery(event)
        query.limit(25).map { it[Memes.meme] }.sorted()
    }

    private fun buildQuery(event: CommandAutoCompleteInteractionEvent): Query {
        return Memes.slice(Memes.meme).select { Memes.meme like "${event.focusedOption.value}%" }
    }
}


