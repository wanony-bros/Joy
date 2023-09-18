package com.wanony.command.manage

import com.wanony.DB
import com.wanony.command.JoyCommand
import com.wanony.dao.*
import com.wanony.getProperty
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.update

private const val JOY_MOD_NAME = "moderator"
private const val JOY_MOD_ROLE_NAME = "Moderator"
private const val JOY_SERVER_DEFAULT_ROLE_NAME = "Joys"

private const val ADD_OPERATION_NAME = "add"
private const val REMOVE_OPERATION_NAME = "remove"

class AdminCommand(val jda: JDA) : JoyCommand {
    override val commandName: String = "admin"
    override val commandData: CommandData =
        Commands.slash(commandName, "Manage Joy's content").addSubcommandGroups(
            SubcommandGroupData(JOY_MOD_NAME, "Add or remove Joy mods").addSubcommands(
                SubcommandData(ADD_OPERATION_NAME, "Add a moderator")
                    .addOption(OptionType.USER, "user", "user to upgrade to mod.", true),
                SubcommandData(REMOVE_OPERATION_NAME, "Remove a moderator")
                    .addOption(OptionType.USER, "user", "user to downgrade from mod.", true),
            )
        )

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val subcommandGroup = event.subcommandGroup
        val subcommandName = event.subcommandName
        val isAdmin = DB.transaction { User.findById(event.user.idLong)?.admin }
        if (isAdmin == null || isAdmin == false) {
            event.reply("You do not have the moderator privileges required to do this!").setEphemeral(true).queue()
            return
        }
        when(subcommandGroup) {
            JOY_MOD_NAME -> when(subcommandName) {
                ADD_OPERATION_NAME -> addModerator(event)
                REMOVE_OPERATION_NAME -> removeModerator(event)
            }
        }
    }

    private fun addModerator(event: SlashCommandInteractionEvent) = DB.transaction {
        val user = event.getOption("user")!!.asUser
        val userIdToUpgrade = user.idLong
        // first find user and insert them if not added
        Users.insertIgnore { it[Users.id] = userIdToUpgrade }
        // update their permissions to be admin
        val updated = Users.update({ Users.id eq userIdToUpgrade }) {
            it[Users.admin] = true
        } > 0
        // if not updated, let the user know something went wrong
        if (!updated) {
            event.reply("Failed to upgrade user permission, please check the DB").queue()
        } else {
            var response = "Success, upgraded ${user.name} to moderator."
            try {
                user.openPrivateChannel().queue { channel: PrivateChannel ->
                    channel.sendMessage("You have been upgraded to a moderator for Joy. You now have access to the `/manage` commands.").queue()
                }
            } catch (e: ErrorResponseException) {
                response = "Success, upgraded ${user.name} to moderator, but couldn't message them"
            }

            val roleAdded = giveModeratorRole(event)
            if (!roleAdded) {
                event.reply("Failed to change roles, but database updated").setEphemeral(true).queue()
            } else {
                event.reply(response).setEphemeral(true).queue()
            }
        }
    }

    private fun removeModerator(event: SlashCommandInteractionEvent) = DB.transaction {
        val user = event.getOption("user")!!.asUser
        val userIdToUpgrade = user.idLong
        // first find user and insert them if not added
        Users.insertIgnore { it[Users.id] = userIdToUpgrade }
        // update their permissions to be admin
        val updated = Users.update({ Users.id eq userIdToUpgrade }) {
            it[Users.admin] = false
        } > 0
        // if not updated, let the user know something went wrong
        if (!updated) {
            event.reply("Failed to downgrade user permission, please check the DB").queue()
        } else {
            try {
                user.openPrivateChannel().queue { channel: PrivateChannel ->
                    channel.sendMessage("You have been removed as a moderator for Joy. If you think this is a mistake, please get in contact in the support server.").queue()
                }
            } catch (e: ErrorResponseException) {
                event.reply("Success, downgraded ${user.name}, but couldn't message them")
            }

            val roleAdded: Boolean = removeModeratorRole(event)
            if (!roleAdded) {
                event.reply("Failed to change roles, but database updated").setEphemeral(true).queue()
            } else {
                event.reply("Success, downgraded ${user.name}.").setEphemeral(true).queue()
            }
        }
    }

    private fun giveModeratorRole(event: SlashCommandInteractionEvent): Boolean {
        val user = event.getOption("user")!!.asMember
        val roleName =  JOY_MOD_ROLE_NAME // Replace with the actual role name
        val guildId: String = getProperty("testGuild")
        // Get the role by name (you can also use getRolesById if you have the role ID)
        val guild = jda.getGuildById(guildId) // Get the guild where the command is executed
        var added = false
        if (guild != null) {
            val role = guild.getRolesByName(roleName, true).firstOrNull() // Second argument is case-sensitive
            if (role != null && user != null) {
                // Add the role to the user
                try {
                    guild.modifyMemberRoles(user, role).queue {
                        // Role added successfully
                        added = true
                    }
                } catch (e: HierarchyException) {
                    added = false
                }
            }
        }
        return added
    }

    private fun removeModeratorRole(event: SlashCommandInteractionEvent): Boolean {
        val user = event.getOption("user")!!.asMember
        val roleName =  JOY_SERVER_DEFAULT_ROLE_NAME // Replace with the actual role name
        val guildId: String = getProperty("testGuild")
        // Get the role by name (you can also use getRolesById if you have the role ID)
        val guild = jda.getGuildById(guildId) // Get the guild where the command is executed
        var added = false
        if (guild != null) {
            val role = guild.getRolesByName(roleName, true).firstOrNull() // Second argument is case-sensitive
            if (role != null && user != null) {
                // Add the role to the user
                try {
                    guild.modifyMemberRoles(user, role).queue {
                        // Role added successfully
                        added = true
                    }
                } catch (e: HierarchyException) {
                    added = false
                }
            }
        }
        return added
    }

}