package com.wanony.command.gfys

import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.command.gfys.links.LinkProvider.getLink
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.awt.Color
import java.util.concurrent.TimeUnit

class TimerCommand : JoyCommand {
    private val timers: MutableMap<String, Job> = mutableMapOf()

    override val commandName: String = "timer"
    override val commandData: CommandData = Commands.slash(
        commandName, "Get sent links for a short duration",).addSubcommands(
            SubcommandData("start", "Get sent links for a duration of time")
                .addOption(OptionType.INTEGER, "duration", "How long the timer will run (minutes)", false)
                .addOption(OptionType.STRING, "group", "Specify a group to get links from", false, true)
                .addOption(OptionType.STRING, "idol", "Specify an idol you want links from", false, true)
                .addOption(OptionType.INTEGER, "interval", "How often links are sent (seconds)", false)
                .addOption(OptionType.STRING, "tag", "Specify a tag you want links from", false, true),

            SubcommandData("stop", "Stop a currently running command")
                .addOption(OptionType.STRING, "id", "ID of the timer to stop", true)

        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            "start" -> start(event)
            "stop" -> stop(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private suspend fun timerHelper(loops: Int, channel: MessageChannel, groupStr: String?, idol: String?, tag: String?, interval: Int, timerId: String) {
        coroutineScope {
            timers.put(timerId,
                launch {
                    repeat(loops) {
                        val link = getLink(channel.idLong, groupStr, idol, tag)
                        channel.sendMessage(link!!.link).queue()
                        delay(TimeUnit.SECONDS.toMillis(interval.toLong()))
                    }
                }
            )
        }
    }

    private suspend fun start(event: SlashCommandInteractionEvent) {
        val groupStr = event.getOption("group")?.asString
        val idol = event.getOption("idol")?.asString
        val tag = event.getOption("tag")?.asString
        var duration = event.getOption("duration")?.asInt
        var interval = event.getOption("interval")?.asInt
        val channel = event.channel

//        if (event.guild != null) {
//            TODO("check if guild max timer is lower than the duration set by user")
//        }
        if (duration == null) {
            duration = 1
        } else {
            if (duration > 30) { duration = 30 }
        }
        if (interval == null) {
            interval = 10
        } else {
            if (interval > 60) { interval = 60 }
        }
        val loops = (duration * 60) / interval
        var count = 1
        var timerId = 1
        for (timer in timers) {
            if (timer.key.startsWith(event.user.id)) {
                count ++
                val id = timer.key.substringAfterLast("_").toInt()
                if (id > timerId) {
                    timerId = id + 1
                }
                if (count >= 10) {
                    event.replyEmbeds(EmbedBuilder().apply {
                        setTitle("Failed to initiate timer!")
                        setDescription("Too many timers running for user: ${event.user.name}")
                        setColor(Color.RED)
                        setFooter("Error for user ${event.user.name}", event.user.effectiveAvatarUrl)
                    }.build()).queue()
                    return
                }
            }
        }
        val timerKey: String = event.user.id + "_" + timerId
        event.replyEmbeds(EmbedBuilder().apply {
            setTitle("Timer Started!")
            setDescription("$groupStr's $idol for $duration minute(s)!\nTimer ID:")
            setColor(Theme.BLURPLE)
            setFooter("Timer for user ${event.user.name}", event.user.effectiveAvatarUrl)
        }.build()).queue()
        timerHelper(loops, channel, groupStr, idol, tag, interval, timerKey)
    }

    private fun stop(event: SlashCommandInteractionEvent) {
        val timerId = event.getOption("id")
        val userId = event.user.id
        val target = userId + "_" + timerId
        val removed = timers.remove(target)
        if (removed != null) {
            event.replyEmbeds(Theme.successEmbed("Stopped timer of ID: `$timerId`").build()).queue()
        }
        event.replyEmbeds(Theme.errorEmbed("Failed to stop timer of ID: `$timerId`.\nPlease check this is running.").build()).queue()
    }

}
