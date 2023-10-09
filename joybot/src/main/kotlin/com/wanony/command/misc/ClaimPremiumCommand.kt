package com.wanony.command.misc

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.Users
import com.wanony.getProperty
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.update
import java.awt.Color

private const val PREMIUM_SUBSCRIPTION_NAME = "premium"


class ClaimPremiumCommand : JoyCommand {
    override val commandName: String = "claim"
    override val commandData: CommandData =
        Commands.slash(commandName, "Manage Joy's content").addSubcommands(
            SubcommandData(PREMIUM_SUBSCRIPTION_NAME, "Claim Joy's premium commands!"))

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subcommandName = event.subcommandName
        // Leaving this as a when for whenever more is added to the claiming command
        when(subcommandName) {
            PREMIUM_SUBSCRIPTION_NAME -> claimPremium(event)
            }
    }

    private fun claimPremium(event: SlashCommandInteractionEvent) = DB.transaction {
        val userId = event.user.idLong
        val joyServerId: Long  = getProperty<String>("testGuild").toLong() // this should be correct server for test/prod
        // Check if this is in the JoyBot server
        if (event.guild!!.idLong != joyServerId) {
            event.replyEmbeds(Theme.errorEmbed("This command needs to be used in the Joy support server!")
                    .build()).setEphemeral(true).queue()
            return@transaction
        }
        // Check if the user has the correct role from Patreon
        val premiumRoleId = 1159465237561475142
        val premiumTestRoleId = 1153262194562183178
        if (event.member!!.roles.any { it.idLong == premiumRoleId || it.idLong == premiumTestRoleId}) {
            Users.insertIgnoreAndGetId { it[Users.id] = userId }
            val madePremium = Users.update({ Users.id eq userId }) { it[Users.isPremium] = true } > 0
            if (!madePremium) {
                event.replyEmbeds(Theme.errorEmbed("Failed to add premium commands! Please contact support in Joy's support server")
                    .build()).setEphemeral(true).queue()
                return@transaction
            }
            event.replyEmbeds(premiumSuccessEmbed(event).build()).setEphemeral(true).queue()
            return@transaction
        } else {
            event.replyEmbeds(Theme.errorEmbed("No $PREMIUM_SUBSCRIPTION_NAME role found! Have you linked Patreon to your discord account?")
                .build()).setEphemeral(true).queue()
            return@transaction
        }
        // If both checks are passed, then give the user the privileges in the DB.
    }

    private fun premiumSuccessEmbed(event: SlashCommandInteractionEvent) : EmbedBuilder = EmbedBuilder().apply {
        setTitle("Successfully Upgraded to Premium!")
        setThumbnail(event.user.effectiveAvatarUrl)
        setDescription("""Thank you ${event.user.name}, you now have access to the premium commands!
            More details can be found in the JoyBot support server!
        """.trimMargin())
        setColor(Color(255, 215, 0))
    }

}