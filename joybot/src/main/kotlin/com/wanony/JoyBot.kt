package com.wanony

import com.wanony.command.AutocompleteProvider
import com.wanony.command.JoyCommand
import com.wanony.command.autocomplete.GroupAutocompleteProvider
import com.wanony.command.autocomplete.MemberAutocompleteProvider
import com.wanony.command.autocomplete.TagAutocompleteProvider
import com.wanony.command.gfys.AddLinkCommand
import com.wanony.command.gfys.GfyCommand
import com.wanony.command.gfys.RandomLinkCommand
import com.wanony.command.gfys.TimerCommand
import com.wanony.command.manage.ManageCommand
import com.wanony.command.memes.MemeCommand
import com.wanony.command.misc.AvatarCommand
import com.wanony.command.misc.InformationCommand
import com.wanony.command.misc.SuggestCommand
import com.wanony.command.reddit.RedditCommand
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.intents
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class JoyBot(
    val jda: JDA,
    private val commands: Map<String, JoyCommand>,
    private val autoCompleteProviders: List<AutocompleteProvider>,
) {
    init {
        updateCommands()
    }

    suspend fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commands[event.name]?.let {
            it.execute(event)
            return
        }

        // No command was found
        event.reply("I can't handle that command right now.").setEphemeral(true).queue()
    }

    fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        autoCompleteProviders.firstOrNull {
            it.autoComplete(event)
        }
    }

    private fun updateCommands() {
        listOfNotNull(
            jda.updateCommands(),
            jda.getGuildById(getProperty<String>("testGuild"))?.updateCommands()
        ).forEach { commandUpdateAction ->
            commands.values.forEach {
                commandUpdateAction.addCommands(it.commandData)
            }
            commandUpdateAction.queue()
        }
    }

}

fun main() {
    val token = getProperty<String>("discordAPIToken")


    val allAutocompleteProviders : List<AutocompleteProvider> = listOf(
        GroupAutocompleteProvider(),
        MemberAutocompleteProvider(),
        TagAutocompleteProvider(),
    )

    val jda = light(token, enableCoroutines = true) {
        intents += listOf(GatewayIntent.GUILD_MEMBERS)
    }

    val allCommands : Map<String, JoyCommand> = listOf(
        AvatarCommand(),
        SuggestCommand(),
        AddLinkCommand(),
        RandomLinkCommand(),
        ManageCommand(),
        GfyCommand(),
        InformationCommand(),
        TimerCommand(),
        MemeCommand(),
        RedditCommand(jda),
    ).associateBy { it.commandName }

    val joy = JoyBot(jda, allCommands, allAutocompleteProviders)
    jda.listener<SlashCommandInteractionEvent> { event ->
        joy.onSlashCommandInteraction(event)
    }
    jda.listener<CommandAutoCompleteInteractionEvent> { event ->
        joy.onCommandAutoCompleteInteraction(event)
    }

}

/**
 * Attempts to find the property given by [key] within misc.properties.
 *
 * @return the value of the property as [T] or null if not found.
 */
@Suppress("UNCHECKED_CAST")
fun <T> findProperty(key: String): T? {
    val props = ClassLoader.getSystemClassLoader().getResourceAsStream("misc.properties").use {
        it ?: throw java.lang.RuntimeException("Missing properties file misc.properties")
        Properties().apply { load(it) }
    }
    return (props.getProperty(key) as T)

}

/**
 * Attempts to get the property given by [key] within misc.properties.
 *
 * Throws a runtime exception is the property does not exist.
 *
 * @return the value of the property
 */
fun <T> getProperty(key: String): T =
    findProperty<T>(key) ?: throw java.lang.RuntimeException("Could not find property with key '$key' in misc.properties")

