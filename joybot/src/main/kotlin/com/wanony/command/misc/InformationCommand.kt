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
import java.time.LocalDate
import java.time.Period


private val NATIONALITY_MAP = mapOf(
    "South Korea" to "🇰🇷",
    "USA" to "🇺🇸",
    "India" to "🇮🇳",
    "Japan" to "🇯🇵",
    "China" to "🇨🇳",
    "Philippines" to "🇵🇭",
    "Taiwan" to "🇹🇼",
    "Thailand" to "🇹🇭",
    "Indonesia" to "🇮🇩",
    "Australia" to "🇦🇺",
    "Hong Kong" to "🇭🇰",
    "Belgium" to "🇧🇪",
    "Canada" to "🇨🇦",
    "Brazil" to "🇧🇷",
    "Vietnam" to "🇻🇳",
    "Russia" to "🇷🇺"
)


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
            // all the groups are given as information
            val groups = getGroupsFromDB()
            embed.apply {
                setTitle("Groups:")
                setDescription(groups)
                setColor(Theme.BLURPLE)
                setFooter("Try /info group:<group> for more information!")
            }
        }
        if (groupStr != null  && idol == null) {
            // Group information
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
            // group and idol information
            val desc = getMemberFromGroupDB(groupStr, idol)

            embed.apply {
                setTitle("${groupStr}'s ${idol}")
                setDescription(desc)
                setColor(Theme.BLURPLE)
//                setFooter(NATIONALITY_MAP[memberRow[Members.country]])
            }
        }
        if (tag != null) {
            // TODO expand on this for when tags are working, and can be searched with groups and members included.
            embed.apply {
                setTitle("")
                setDescription("")
                setColor(Theme.BLURPLE)
                setFooter("")
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

    private fun getMemberFromGroupDB(group: String, member: String): String = DB.transaction {
        val memberRow = (Groups.innerJoin(Members)).slice(
            Groups.romanName,
            Members.romanStageName,
            Members.romanFullName,
            Members.country,
            Members.cityOfBirth,
            Members.dateOfBirth,
            Members.hangulFullName,
            // Expand later to include tags
        ).select {
            (Groups.romanName eq group) and
                    (Members.romanStageName eq member)
        }.groupBy(
            Groups.romanName,
            Members.romanStageName,
            Members.romanFullName,
            Members.country,
            Members.cityOfBirth,
            Members.dateOfBirth,
            Members.hangulFullName
        ).first()

        val linkCount = (Members.innerJoin(LinkMembers)).slice(
            LinkMembers.memberId
        ).select { Members.romanStageName eq member }.count()
        val age: Int = Period.between(memberRow[Members.dateOfBirth], LocalDate.now()).years
        val descriptionBuilder = StringBuilder()

        // Stage Name
        descriptionBuilder.appendLine("Stage Name: **${memberRow[Members.romanStageName]}**")

        // Name
        if (memberRow[Members.romanFullName]?.isNotBlank() == true) {
            descriptionBuilder.appendLine("Name: ${memberRow[Members.romanFullName]}")
        }

        // Age
        descriptionBuilder.appendLine("Age: $age years old")

        // Nationality
        if (memberRow[Members.country]?.isNotBlank() == true) {
            val nationalityDescription = NATIONALITY_MAP[memberRow[Members.country]]
            if (nationalityDescription != null) {
                descriptionBuilder.appendLine("Nationality: ${memberRow[Members.country]} $nationalityDescription")
            } else {
                descriptionBuilder.appendLine("Nationality: ${memberRow[Members.country]}")
            }
        }

        // City of Birth
        if (memberRow[Members.cityOfBirth]?.isNotBlank() == true) {
            descriptionBuilder.appendLine("City of Birth: ${memberRow[Members.cityOfBirth]}")
        }

        // Links Added
        descriptionBuilder.appendLine("Links Added: `$linkCount`")

        descriptionBuilder.toString()
    }

    private fun getGroupsFromDB(): String = DB.transaction {
        Groups.slice(Groups.romanName).selectAll().sortedBy { Groups.romanName }
        .joinToString { "`" + it[Groups.romanName] + "`" }
    }
}