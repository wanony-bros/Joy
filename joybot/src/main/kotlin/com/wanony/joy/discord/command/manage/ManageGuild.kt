package com.wanony.joy.discord.command.manage

import com.wanony.joy.data.DB
import com.wanony.joy.data.dao.Guilds
import com.wanony.joy.discord.command.JoyCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

private const val TIMER_OPERATION_NAME = "timer"
private const val LIMIT_OPERATION_NAME = "limit"

class ManageGuildCommand : JoyCommand {
    override val commandName: String = "server"
    override val commandData: CommandData =
        Commands.slash(commandName, "Manage Joy's settings in your server.").addSubcommandGroups(
            SubcommandGroupData(TIMER_OPERATION_NAME, "Manage timers in your server.")
                .addSubcommands(
                    SubcommandData(LIMIT_OPERATION_NAME, "Set the timer limit.")
                        .addOption(OptionType.INTEGER, "limit", "The new timer limit (minutes)", true)
                )
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subcommandGroup = event.subcommandGroup
        val subcommandName = event.subcommandName
        when(subcommandGroup) {
            TIMER_OPERATION_NAME -> when(subcommandName) {
                LIMIT_OPERATION_NAME -> updateTimerLimit(event)
            }
        }

    }

    private fun updateTimerLimit(event: SlashCommandInteractionEvent) = DB.transaction {
        if (event.guild == null) {
            event.reply("This command can only be used in servers!").setEphemeral(true).queue()
            return@transaction
        }
        // check user guild permissions
        if (event.member?.hasPermission(event.guildChannel, Permission.ADMINISTRATOR) != true) {
            event.reply("You do not have permission to use this command!").setEphemeral(true).queue()
            return@transaction
        }
        // if guild not in the DB, we insert it here.
        Guilds.insertIgnore {
            it[Guilds.guildId] = event.guild!!.id
            it[Guilds.timerLimit] = 5
        }
        var newLimit = event.getOption("limit")!!.asLong
        if (newLimit < 0) {
            event.reply("Timer limit must be a positive number!")
        } else if (newLimit > 30) {
            newLimit = 30
        }

        val currentTimerLimit = Guilds.select { Guilds.guildId eq event.guild!!.id }
            .firstOrNull()?.getOrNull(Guilds.timerLimit)
        val updated = Guilds.update({ Guilds.guildId eq event.guild!!.id }) {
            it[Guilds.timerLimit] = newLimit
        } > 0
        if (!updated) {
            event.reply("Failed to update timer limit, please message in the support server.").setEphemeral(true).queue()
        } else {
            val emb = dev.minn.jda.ktx.messages.EmbedBuilder().apply {
                this.title = "Success"
                this.description = "Timer Limit for ${event.guild!!.name} updated from `$currentTimerLimit` minutes to `$newLimit` minutes"
                this.image = event.guild!!.iconUrl
                this.footer {
                    this.name = "Updated by ${event.user.name}"
                    this.iconUrl = event.user.effectiveAvatarUrl
                }
            }.build()

            event.replyEmbeds(emb).setEphemeral(true).queue()
        }

    }
}