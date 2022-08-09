package com.wanony.command.auditing

import com.wanony.DB
import com.wanony.Theme
import com.wanony.checkGuildReplyPermissions
import com.wanony.command.JoyCommand

import com.wanony.dao.AuditingChannels


import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore

private const val ADD_OPERATION_NAME = "add"
private const val REMOVE_OPERATION_NAME = "remove"


class AuditingCommand(val jda: JDA) : JoyCommand {
    override val commandName: String = "auditing"
    override val commandData: CommandData = Commands.slash(
        commandName, "See when new links are added to Joy!"
    )
        .addSubcommands(
            SubcommandData(ADD_OPERATION_NAME, "Add auditing to a channel"),
            SubcommandData(REMOVE_OPERATION_NAME, "Remove auditing from a channel")
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        when(event.subcommandName) {
            ADD_OPERATION_NAME -> addAuditing(event)
            REMOVE_OPERATION_NAME -> removeAuditing(event)
            else -> throw java.lang.RuntimeException("WTF DISCORD????")
        }
    }

    private fun addAuditing(event: SlashCommandInteractionEvent) {
        val channel = event.channel as? GuildMessageChannel ?: return replyGuildRequiredError(event)

        if (event.checkGuildReplyPermissions()) {
            event.replyEmbeds(
                Theme.errorEmbed(
                    """Joy does not have permission to send message in ${channel.name}
                        Please update the permissions and try again!
                    """.trimIndent()).build()).setEphemeral(true).queue()
            return
        }

        val inserted = DB.transaction {
            AuditingChannels.insertIgnore {
                it[channelId] = channel.id
            }.insertedCount > 0
        }
        if (!inserted) {
            event.replyEmbeds(Theme.errorEmbed("Failed to add auditing to ${channel.name}. Please check if auditing is already added!").build()).queue()
            return
        }

        event.replyEmbeds(Theme.successEmbed("Auditing updates will be received in this channel!").build()).queue()

    }

    private fun removeAuditing(event: SlashCommandInteractionEvent) {
        val channel = event.channel
        val deleted = DB.transaction {
            AuditingChannels.deleteWhere {
                AuditingChannels.channelId eq channel.id
            }
        }
        if (deleted == 0) {
            event.replyEmbeds(
                Theme.errorEmbed("${channel.name} does not receive auditing updates!").build()).setEphemeral(true).queue()
            return
        }
        event.replyEmbeds(Theme.successEmbed("${channel.name} will no longer receive auditing updates!").build()).setEphemeral(true).queue()
    }


    // TODO create some file that hosts all of these kind of helper commands
    private fun replyGuildRequiredError(event: SlashCommandInteractionEvent) {
        event.replyEmbeds(Theme.errorEmbed("This command can only be used within a server.").build()).setEphemeral(true).queue()
    }
}