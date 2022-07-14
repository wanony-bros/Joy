package com.wanony.command

import com.wanony.Theme
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.internal.utils.PermissionUtil

fun SlashCommandInteractionEvent.replyGuildRequiredError() {
    replyEmbeds(Theme.errorEmbed("This command can only be used within a server.").build()).setEphemeral(true).queue()
}

fun SlashCommandInteractionEvent.checkGuildReplyPermissions() : Boolean {
    val replyChannel = (channel as? GuildMessageChannel) ?: return false
    val joy = guild?.getMember(jda.selfUser) ?: return false
    return !PermissionUtil.checkPermission(replyChannel.permissionContainer, joy, Permission.MESSAGE_SEND)
}

fun SlashCommandInteractionEvent.replyGuildPermissionError() {
    replyEmbeds(
        Theme.errorEmbed(
            """
            Joy does not have permission to send message in ${channel.name}
            Please update the permissions and try again!
            """.trimIndent()).build()).setEphemeral(true).queue()
}