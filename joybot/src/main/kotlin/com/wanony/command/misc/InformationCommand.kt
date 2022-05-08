package com.wanony.command.misc

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.jetbrains.exposed.sql.*

class InformationCommand : JoyCommand{
    override val commandName: String = "info"
    override val commandData: CommandData = Commands.slash(
        commandName, "Get information on Joy's database!")
        .addOption(OptionType.STRING, "group", "Enter the group", false, true)
        .addOption(OptionType.STRING, "idol", "Enter the idol", false, true)
        .addOption(OptionType.STRING, "tag", "Enter the tag", false, true)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val groupStr = event.getOption("group")?.asString
        val idol = event.getOption("idol")?.asString
        val tag = event.getOption("tag")?.asString
        val embed = EmbedBuilder()

        if (groupStr == null && idol == null && tag == null) {
            val groups = getGroupsFromDB()
            embed.apply {
                setTitle("Groups:")
                setDescription(groups)
                setColor(Theme.BLURPLE)
                setFooter("Try /info group:<group> for more information!")
            }
        }
        if (groupStr != null  && idol == null) {
            val desc = "`Name${" ".repeat(20)}Link Count`"
            val members = getMembersFromGroupDB(groupStr)
            embed.apply {
                setTitle("$groupStr Members")
                setDescription("$desc\n$members")
                setColor(Theme.BLURPLE)
                setFooter("Try /info group:$groupStr idol:<idol> for more information!")
            }
        }
        if (groupStr != null && idol != null) {
            embed.apply {
            }
        }
        if (tag != null) {
            embed.apply {
            }
        }
        event.replyEmbeds(embed.build()).queue()
    }

    private fun getMembersFromGroupDB(group: String): String = DB.transaction {
        (Groups.innerJoin(Members).innerJoin(LinkMembers)).slice(Members.romanStageName, LinkMembers.memberId.count())
            .select { (Groups.romanName eq group) and
                    (Members.groupId eq Groups.id) and
                    (LinkMembers.memberId eq Members.id) }.groupBy(Members.id)
            .toList().sortedBy { Members.romanStageName }.joinToString("\n") {
                "`" + it[Members.romanStageName] +
                    " ".repeat(26 - it[LinkMembers.memberId.count()].toString().length) +
                    it[LinkMembers.memberId.count()] + "`"
            }
    }

    private fun getGroupsFromDB(): String = DB.transaction {
        Groups.slice(Groups.romanName).selectAll().sortedBy { Groups.romanName }
        .joinToString { "`" + it[Groups.romanName] + "`" }
    }
}