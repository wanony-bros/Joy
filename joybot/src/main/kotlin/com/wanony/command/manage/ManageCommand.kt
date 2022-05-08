package com.wanony.command.manage

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.*
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.jetbrains.exposed.sql.and

private const val TAG_GROUP_NAME = "tag"
private const val IDOL_GROUP_NAME = "idol"
private const val GROUP_GROUP_NAME = "group"
private const val LINK_GROUP_NAME = "link"

private const val ADD_OPERATION_NAME = "add"
private const val RENAME_OPERATION_NAME = "rename"
private const val REMOVE_OPERATION_NAME = "remove"

class ManageCommand : JoyCommand {
    override val commandName: String = "manage"
    override val commandData: CommandData =
        Commands.slash(commandName, "Manage Joy's content").addSubcommandGroups(
            SubcommandGroupData(TAG_GROUP_NAME, "Manage tags").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a tag")
                    .addOption(OptionType.STRING, "name", "The tag to add", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a tag")
                    .addOption(OptionType.STRING, "tag", "The current name of the tag to change", true, true)
                    .addOption(OptionType.STRING, "new", "The new name of the tag", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a tag")
                    .addOption(OptionType.STRING, "tag", "The tag to remove", true, true)
            ),
            SubcommandGroupData(IDOL_GROUP_NAME, "Manage idols").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a idol")
                    .addOption(OptionType.STRING, "group", "The group to add the idol to", true, true)
                    .addOption(OptionType.STRING, "name", "The idol to add", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a idol")
                    .addOption(OptionType.STRING, "group", "The group the idol belongs to", true, true)
                    .addOption(OptionType.STRING, "idol", "The current name of the idol to change", true, true)
                    .addOption(OptionType.STRING, "name", "The new name of the idol", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a idol")
                    .addOption(OptionType.STRING, "group", "The group the idol belongs to", true, true)
                    .addOption(OptionType.STRING, "idol", "The idol to remove", true, true)
            ),
            SubcommandGroupData(GROUP_GROUP_NAME, "Manage groups").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a group")
                    .addOption(OptionType.STRING, "name", "The group to add", true),
                SubcommandData(RENAME_OPERATION_NAME, "Rename a group")
                    .addOption(OptionType.STRING, "group", "The current name of the group to change", true, true)
                    .addOption(OptionType.STRING, "new", "The new name of the group", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a group")
                    .addOption(OptionType.STRING, "group", "The group to remove", true, true)
            ),
            SubcommandGroupData(LINK_GROUP_NAME, "Manage links").addSubcommands(
                SubcommandData("remove", "Remove a link")
                    .addOption(OptionType.STRING, "link", "The link to remove", true)
            )
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
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
        if (name.contains(' ')) {
            event.replyEmbeds(Theme.errorEmbed("Tags must not contain spaces.").build()).queue()
            return
        }

        val error: String? = DB.transaction {
            val tag = Tag.find { Tags.tagName eq name }.firstOrNull()
            if (tag != null) {
                return@transaction "Tag `$name` already exists!"
            }

            Tag.new {
                this.tagName = name
                this.addedBy = event.user.idLong
            }
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Added tag `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun renameTag(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")!!.asString
        val new = event.getOption("new")!!.asString
        val error: String? = DB.transaction {
            val newTag = Tag.find { Tags.tagName eq new }.firstOrNull()
            if (newTag != null) {
                return@transaction "Tag `$new` already exists!"
            }

            Tag.find { Tags.tagName eq name }.firstOrNull()?.let {
                it.tagName = new
            } ?: return@transaction "Tag `$name` doesn't exist."
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
                ?: return@transaction "Tag `$name` doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Deleted the tag `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun addIdol(event: SlashCommandInteractionEvent) {
        val group = event.getOption("group")!!.asString
        val name = event.getOption("name")!!.asString
        val error: String? = DB.transaction {
            val grp = Group.find { Groups.romanName eq group }.firstOrNull()
                ?: return@transaction "Group `$group` doesn't exist."

            val member = Member.find { (Members.groupId eq grp.id) and (Members.romanName eq name) }
            if (member.firstOrNull() != null) {
                return@transaction "Idol `$name` of `$group` already exists"
            }

            Member.new {
                this.romanName = name
                this.groupId = grp
                this.addedBy = event.user.idLong
            }

            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Added idol `$name` to group `$group`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun renameIdol(event: SlashCommandInteractionEvent) {
        val group = event.getOption("group")!!.asString
        val name = event.getOption("idol")!!.asString
        val new = event.getOption("name")!!.asString
        val error: String? = DB.transaction {
            val grp = Group.find { Groups.romanName eq group }.firstOrNull()
                ?: return@transaction "Group `$group` doesn't exist."

            val member = Member.find { (Members.groupId eq grp.id) and (Members.romanName eq name) }.firstOrNull()
                ?: return@transaction "Idol `$name` of `$group` doesn't exist"

            val newMember = Member.find { (Members.groupId eq grp.id) and (Members.romanName eq new) }.firstOrNull()
            if (newMember != null) {
                return@transaction "Idol `$new` of `$group` already exists"
            }

            member.romanName = new
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Renamed idol `$name` or group `$group` to `$new`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun removeIdol(event: SlashCommandInteractionEvent) {
        val group = event.getOption("group")!!.asString
        val name = event.getOption("idol")!!.asString
        val error: String? = DB.transaction {
            val grp = Group.find { Groups.romanName eq group }.firstOrNull()
                ?: return@transaction "Group `$group` doesn't exist."

            val member = Member.find { (Members.groupId eq grp.id) and (Members.romanName eq name) }.firstOrNull()
                ?: return@transaction "Idol `$name` of `$group` doesn't exist."

            member.delete()
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Deleted idol `$name` from group `$group`.")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun addGroup(event: SlashCommandInteractionEvent) {
        val name = event.getOption("name")!!.asString
        val error: String? = DB.transaction {
            val tag = Group.find { Groups.romanName eq name }.firstOrNull()
            if (tag != null) {
                return@transaction "Group `$name` already exists!"
            }

            Group.new {
                this.romanName = name
                this.addedBy = event.user.idLong
            }
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Added group `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun renameGroup(event: SlashCommandInteractionEvent) {
        val old = event.getOption("group")!!.asString
        val new = event.getOption("new")!!.asString
        val error: String? = DB.transaction {
            val newGroup = Group.find { Groups.romanName eq new }.firstOrNull()
            if (newGroup != null) {
                return@transaction "Group `$new` already exists!"
            }

            Group.find { Groups.romanName eq old }.firstOrNull()?.let {
                it.romanName = new
            } ?: return@transaction "Group `$old` doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Renamed group `$old` to `$new`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun removeGroup(event: SlashCommandInteractionEvent) {
        val name = event.getOption("group")!!.asString
        val error: String? = DB.transaction {
            Group.find { Groups.romanName eq name }.firstOrNull()?.delete()
                ?: return@transaction "Group `$name` doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Removed group `$name`")

        event.replyEmbeds(builder.build()).queue()
    }

    private fun removeLink(event: SlashCommandInteractionEvent) {
        val link = event.getOption("link")!!.asString
        val error: String? = DB.transaction {
            Link.find { Links.link eq link }.firstOrNull()?.delete()
                ?: return@transaction "Link doesn't exist."
            null
        }

        val builder = if (error != null)
            Theme.errorEmbed(error)
        else
            Theme.successEmbed("Removed link <$link>!")

        event.replyEmbeds(builder.build()).queue()
    }
}