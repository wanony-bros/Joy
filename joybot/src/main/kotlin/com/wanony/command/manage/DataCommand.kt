package com.wanony.command.manage

import com.wanony.DB
import com.wanony.command.CustomButton
import com.wanony.command.JoyCommand
import com.wanony.dao.*
import com.wanony.getProperty
import dev.minn.jda.ktx.messages.InlineEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update


private const val VIEW_DATA_NAME = "view"
private const val DELETE_DATA_NAME = "delete"
private const val DELETE_BUTTON_ID = "deleteData"
private const val KEEP_BUTTON_ID = "keepData"

class DeleteButton() : CustomButton {
    override val buttonId: String = DELETE_BUTTON_ID

    override suspend fun execute(event: ButtonInteractionEvent) = DB.transaction {
        val userId = event.user.idLong
        val u = Users.deleteWhere { Users.id eq userId } > 0
        val m = Memes.deleteWhere { Memes.addedBy eq userId } > 0
        val l = Links.update({ Links.addedBy eq userId }) {
            it[Links.addedBy] = getProperty<Long>("wanonyId")
        } > 0
        val t = Tags.update({ Tags.addedBy eq userId }) {
            it[Tags.addedBy] = getProperty<Long>("wanonyId")
        } > 0
        val me = Members.update({ Members.addedBy eq userId }) {
            it[Members.addedBy] = getProperty<Long>("wanonyId")
        } > 0
        val g = Groups.update({ Groups.addedBy eq userId }) {
            it[Groups.addedBy] = getProperty<Long>("wanonyId")
        } > 0
        if (u || m || l || t || me || g) {
            event.reply("Successfully deleted all user data.").setEphemeral(true).queue()
        } else {
            event.reply("Something went wrong, ").setEphemeral(true).queue()
        }
    }
}

class KeepDataButton() : CustomButton {
    override val buttonId: String = KEEP_BUTTON_ID

    override suspend fun execute(event: ButtonInteractionEvent) {
        event.reply("Your data has **not** been deleted.").setEphemeral(true).queue()
    }
}

class DataCommand : JoyCommand {
    override val commandName: String = "data"
    override val commandData: CommandData =
        Commands.slash(commandName, "View and delete your data on Joy").addSubcommands(
            SubcommandData(VIEW_DATA_NAME, "View all your data stored on Joy.")
        ).addSubcommands(
            SubcommandData(DELETE_DATA_NAME, "Delete all your data stored on Joy.")
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subCommand = event.subcommandName
        val partialUserDataEmbed = pullAllUserData(event.user.idLong)
        when (subCommand) {
            VIEW_DATA_NAME -> viewData(event, partialUserDataEmbed)
            DELETE_DATA_NAME -> deleteData(event, partialUserDataEmbed)
        }
    }

    private fun viewData(event: SlashCommandInteractionEvent, partialEmbed: InlineEmbed?) {
        // provide the user all of their data stored on Joy.
        val userId = event.user.idLong
        if (partialEmbed != null) {
            event.replyEmbeds(partialEmbed.build()).queue()
        } else {
            val e = dev.minn.jda.ktx.messages.EmbedBuilder().apply {
                this.title = "User ID: `$userId`"
                this.description = "No data stored."
            }.build()
            event.replyEmbeds(e).queue()
        }
    }

    private fun deleteData(event: SlashCommandInteractionEvent, partialEmbed: InlineEmbed?) {
        // delete the data stored for that user, first prompt them if they are sure.
        val userId = event.user.idLong
        if (partialEmbed != null) {
            event.channel.sendMessageEmbeds(partialEmbed.build()).queue()
            event.reply("Are you sure you want to delete your data? (It cannot be recovered)")
                .addActionRow(
                    Button.primary(KEEP_BUTTON_ID, "Keep my data"),
                    Button.danger(DELETE_BUTTON_ID, "Delete My data"),
                ).setEphemeral(true).queue()
        } else {
            val e = dev.minn.jda.ktx.messages.EmbedBuilder().apply {
                this.title = "User ID: `$userId`"
                this.description = "No data stored."
            }.build()
            event.channel.sendMessageEmbeds(e).queue()
        }
    }

    private fun pullAllUserData(userId: Long): InlineEmbed? = DB.transaction {
        val user = User.findById(userId)
        if (user != null) {
            val linksAdded = Links.select { Links.addedBy eq userId }.map {
                it[Links.link]
            }
            val memesAdded = Memes.select { Memes.addedBy eq userId }.map {
                it[Memes.meme]
            }
            val tagsAdded = Tags.select { Tags.addedBy eq userId }.map {
                it[Tags.tagName]
            }
            val membersAdded = Members.select { Members.addedBy eq userId }.map {
                it[Members.romanStageName]
            }
            val groupsAdded = Groups.select { Groups.addedBy eq userId }.map {
                it[Groups.romanName]
            }

            val description = buildString {
                appendLine("Links added (contribution): ${linksAdded.size}")
                appendLine(memesAdded.joinToStringOrNull(", "))
                appendLine(tagsAdded.joinToStringOrNull(", "))
                appendLine(membersAdded.joinToStringOrNull(", "))
                appendLine(groupsAdded.joinToStringOrNull("`, `"))
            }.trim()

            return@transaction dev.minn.jda.ktx.messages.EmbedBuilder().apply {
                this.title = "User ID: `$userId`"
                this.description = description
            }

        } else {
            return@transaction null
        }
    }

    private fun <T> List<T>?.joinToStringOrNull(separator: String): String? {
        return this?.takeIf { it.isNotEmpty() }?.joinToString(separator)
    }
}