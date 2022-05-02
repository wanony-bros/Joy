package com.wanony

import com.wanony.command.allCommands
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*

class JoyBot : ListenerAdapter() {
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        allCommands[event.name]?.let {
            it.execute(event)
            return
        }

        // No command was found
        event.reply("I can't handle that command right now.").setEphemeral(true).queue()
    }
}

fun main() {
    val token = getProperty<String>("discordAPIToken")
    val jda = JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MEMBERS))
        .addEventListeners(JoyBot())
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