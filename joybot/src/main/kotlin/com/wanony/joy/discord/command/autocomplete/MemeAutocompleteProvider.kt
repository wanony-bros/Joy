package com.wanony.joy.discord.command.autocomplete

import com.wanony.joy.data.DB
import com.wanony.joy.data.dao.Memes
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.select

class MemeAutocompleteProvider : com.wanony.joy.discord.command.AutocompleteProvider {

    override val eventName: String = "meme"
    override val commandSelector: com.wanony.joy.discord.command.CommandSelector =
        com.wanony.joy.discord.command.ALL_COMMANDS

    override fun provideOptions(event: CommandAutoCompleteInteractionEvent): List<String> = DB.transaction {
        val query = buildQuery(event)
        query.limit(25).map { it[Memes.meme] }.sorted()
    }

    private fun buildQuery(event: CommandAutoCompleteInteractionEvent): Query {
        return Memes.slice(Memes.meme).select { Memes.meme like "${event.focusedOption.value}%" }
    }
}


