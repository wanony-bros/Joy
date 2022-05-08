package com.wanony.command.manage

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.Tag
import com.wanony.dao.Tags
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

private const val TAG_GROUP_NAME = "tag"
private const val IDOL_GROUP_NAME = "idol"
private const val GROUP_GROUP_NAME = "group"
private const val LINK_GROUP_NAME = "link"

private const val ADD_OPERATION_NAME = "add"
private const val RENAME_OPERATION_NAME = "rename"
private const val REMOVE_OPERATION_NAME = "remove"

class ManageCommand : JoyCommand {
    override val name: String = "manage"
    override val commandData: CommandData =
        Commands.slash(name, "Manage Joy's content").addSubcommandGroups(
            SubcommandGroupData(TAG_GROUP_NAME, "Manage tags").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a tag")
                    .addOption(OptionType.STRING, "name", "The tag to add", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a tag")
                    .addOption(OptionType.STRING, "name", "The current name of the tag to change", true)
                    .addOption(OptionType.STRING, "new", "The new name of the tag", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a tag")
                    .addOption(OptionType.STRING, "name", "The tag to remove", true)
            ),
            SubcommandGroupData(IDOL_GROUP_NAME, "Manage idols").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a idol")
                    .addOption(OptionType.STRING, "name", "The idol to add", true)
                    .addOption(OptionType.STRING, "group", "The group to add the idol to", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a idol")
                    .addOption(OptionType.STRING, "name", "The current name of the idol to change", true)
                    .addOption(OptionType.STRING, "group", "The group the idol belongs to", true)
                    .addOption(OptionType.STRING, "new", "The new name of the idol", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a idol")
                    .addOption(OptionType.STRING, "idol", "The idol to remove", true)
            ),
            SubcommandGroupData(GROUP_GROUP_NAME, "Manage groups").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a group")
                    .addOption(OptionType.STRING, "name", "The group to add", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a group")
                    .addOption(OptionType.STRING, "name", "The current name of the group to change", true)
                    .addOption(OptionType.STRING, "new", "The new name of the group", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a group")
                    .addOption(OptionType.STRING, "name", "The group to remove", true)
            ),
            SubcommandGroupData(LINK_GROUP_NAME, "Manage links").addSubcommands(
                SubcommandData("remove", "Remove a link")
                    .addOption(OptionType.STRING, "name", "The link to remove", true)
            )
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val subcommandGroup = event.subcommandGroup
        val subcommandName = event.subcommandName
        when(subcommandGroup) {
            TAG_GROUP_NAME -> when(subcommandName) {
                ADD_OPERATION_NAME -> addTag(event)
                RENAME_OPERATION_NAME -> renameTag(event)
                REMOVE_OPERATION_NAME -> removeTag(event)
                else -> throw java.lang.RuntimeException("WTF DISCORD????")
            }
            IDOL_GROUP_NAME -> when(subcommandName) {
                ADD_OPERATION_NAME -> addIdol(event)
                RENAME_OPERATION_NAME -> renameIdol(event)
                REMOVE_OPERATION_NAME -> removeIdol(event)
                else -> throw java.lang.RuntimeException("WTF DISCORD????")
            }
            GROUP_GROUP_NAME -> when(subcommandName) {
                ADD_OPERATION_NAME -> addGroup(event)
                RENAME_OPERATION_NAME -> renameGroup(event)
                REMOVE_OPERATION_NAME -> removeGroup(event)
                else -> throw RuntimeException("WTF DISCORD????")
            }
            LINK_GROUP_NAME -> when(subcommandName) {
                REMOVE_OPERATION_NAME -> removeLink(event)
                else -> throw RuntimeException("WTF DISCORD????")
            }
        }
    }

    private fun addTag(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")!!.asString
        val error: String? = DB.transaction {
            Tag.find { Tags.tagName eq name }.firstOrNull() ?: return@transaction "Tag `$name` already exists!"

            Tag.new {
                this.tagName = name
                this.addedBy = event.user.idLong
            }
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Added tag: `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun renameTag(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")!!.asString
        val new = event.getOption("new")!!.asString
        val error: String? = DB.transaction {
            val newTag = Tag.find { Tags.tagName eq new }.firstOrNull()
            if (newTag != null) {
                return@transaction "Tag with name `$new` already exists"
            }

            Tag.find { Tags.tagName eq name }.firstOrNull()?.let {
                it.tagName = new
            } ?: return@transaction "Tag with name `$name` doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Renamed tag `$name` to `$new`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun removeTag(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")!!.asString
        val error: String? = DB.transaction {

            Tag.find { Tags.tagName eq name }.firstOrNull()?.delete()
                ?: return@transaction "Tag with name `$name` doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Deleted the tag `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    fun addIdol(event: SlashCommandInteractionEvent) {

    }

    fun renameIdol(event: SlashCommandInteractionEvent) {

    }

    fun removeIdol(event: SlashCommandInteractionEvent) {

    }

    fun addGroup(event: SlashCommandInteractionEvent) {

    }

    fun renameGroup(event: SlashCommandInteractionEvent) {

    }

    fun removeGroup(event: SlashCommandInteractionEvent) {

    }

    fun removeLink(event: SlashCommandInteractionEvent) {

    }
}