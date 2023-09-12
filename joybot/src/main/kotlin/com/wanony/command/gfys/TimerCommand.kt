package com.wanony.command.gfys

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.command.gfys.links.LinkProvider.getLink
import com.wanony.dao.Guilds
import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.select
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
        val duration = event.getOption("duration")?.asInt
        var interval = event.getOption("interval")?.asInt
        val channel = event.channel

        var updatedDuration = duration?.coerceAtMost(30) ?: 1

        event.guild?.let { guild ->
            DB.transaction {
                Guilds.select { Guilds.guildId eq guild.id }.firstOrNull()?.let { row ->
                    val timerLimit = row[Guilds.timerLimit].toInt()
                    updatedDuration = updatedDuration.coerceAtMost(timerLimit)
                }
            }
        }

        interval = interval?.coerceAtMost(60) ?: 10
        val loops = (updatedDuration * 60) / interval
        val userTimers = timers.keys.filter { it.startsWith(event.user.id) }

        if (userTimers.size >= 10) {
            event.replyEmbeds(buildErrorEmbed(event.user, "Too many timers running for user: ${event.user.name}")).queue()
            return
        }

        val timerId = userTimers.size + 1
        val readableTimerId = "${event.user.name}-${timerId}"
        val timerKey = "${event.user.id}_$timerId"

        event.replyEmbeds(buildSuccessEmbed(event.user, "$groupStr's $idol for $updatedDuration minute(s)!\nTimer ID: $readableTimerId")).queue()
        timerHelper(loops, channel, groupStr, idol, tag, interval, timerKey)
    }

    private fun buildErrorEmbed(user: User, description: String) = EmbedBuilder().apply {
        setTitle("Failed to initiate timer!")
        setDescription(description)
        setColor(Color.RED)
        setFooter("Error for user ${user.name}", user.effectiveAvatarUrl)
    }.build()

    private fun buildSuccessEmbed(user: User, description: String) = EmbedBuilder().apply {
        setTitle("Timer Started!")
        setDescription(description)
        setColor(Theme.BLURPLE)
        setFooter("Timer for user ${user.name}", user.effectiveAvatarUrl)
    }.build()


    private fun stop(event: SlashCommandInteractionEvent) {
        val readableTimerId = event.getOption("id")?.asString

        if (readableTimerId == null) {
            event.replyEmbeds(buildErrorEmbed(event.user, "Invalid timer ID. Please provide a valid timer ID.")).queue()
        } else {
            val userId = event.user.id
            val target = timers.keys.find { it.startsWith(userId) && it.endsWith("_${readableTimerId.split("-").last()}") }
            if (target != null) {
                timers.remove(target)
                event.replyEmbeds(buildSuccessEmbed(event.user, "Stopped timer of ID: `$readableTimerId`")).queue()
            } else {
                event.replyEmbeds(buildErrorEmbed(event.user, "Failed to stop timer of ID: `$readableTimerId`.\nPlease check this is running.")).queue()
            }
        }
    }


}
