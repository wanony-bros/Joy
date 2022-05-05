package com.wanony.command.manage

import com.wanony.command.JoyBotCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData

class ManageCommand : JoyBotCommand {
    override val name: String = "manage"
    override val commandData: CommandData =
        Commands.slash(name, "Manage Joy's content").addSubcommandGroups(
            SubcommandGroupData("tag", "Manage tags").addSubcommands(

            ),
            SubcommandGroupData("member", "Manage members").addSubcommands(

            ),
            SubcommandGroupData("groups", "Manage groups").addSubcommands(

            ),
            SubcommandGroupData("links", "Manage links").addSubcommands(

            )
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        TODO("Not yet implemented")
    }
}