package com.wanony

import com.wanony.command.AutocompleteProvider
import com.wanony.command.JoyCommand
import com.wanony.command.autocomplete.GroupAutocompleteProvider
import com.wanony.command.autocomplete.MemberAutocompleteProvider
import com.wanony.command.autocomplete.TagAutocompleteProvider
import com.wanony.command.gfys.AddLinkCommand
import com.wanony.command.gfys.GfyCommand
import com.wanony.command.gfys.RandomLinkCommand
import com.wanony.command.manage.ManageCommand
import com.wanony.command.misc.AvatarCommand
import com.wanony.command.misc.SuggestCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class JoyBot(
    private val commands: Map<String, JoyCommand>,
    private val autoCompleteProviders: List<AutocompleteProvider>
) : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commands[event.name]?.let {
            it.execute(event)
            return
        }

        // No command was found
        event.reply("I can't handle that command right now.").setEphemeral(true).queue()
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        autoCompleteProviders.firstOrNull {
            it.autoComplete(event)
        }
    }
}

fun main() {
    val token = getProperty<String>("discordAPIToken")

    val allCommands : Map<String, JoyCommand> = listOf(
        AvatarCommand(),
        SuggestCommand(),
        AddLinkCommand(),
        RandomLinkCommand(),
        ManageCommand(),
        GfyCommand(),
    ).associateBy { it.commandName }

    val allAutocompleteProviders : List<AutocompleteProvider> = listOf(
        GroupAutocompleteProvider(),
        MemberAutocompleteProvider(),
        TagAutocompleteProvider(),
    )

    val jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MEMBERS))
        .addEventListeners(JoyBot(allCommands, allAutocompleteProviders))
        .build()

    listOfNotNull(
        jda.updateCommands(),
        jda.getGuildById(getProperty<String>("testGuild"))?.updateCommands()
    ).forEach { commands ->
        allCommands.values.forEach {
            commands.addCommands(it.commandData)
        }

        commands.queue()
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> getProperty(key: String): T {
    val props = ClassLoader.getSystemClassLoader().getResourceAsStream("misc.properties").use {
        Properties().apply { load(it) }
    }
    return (props.getProperty(key) as T)
        ?: throw java.lang.RuntimeException("Could not find property with key '$key' in misc.properties")
}