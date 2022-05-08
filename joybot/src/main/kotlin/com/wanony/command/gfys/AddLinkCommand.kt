package com.wanony.command.gfys

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
import org.jetbrains.exposed.sql.transactions.transaction

class AddLinkCommand : JoyCommand {
    override val commandName: String = "addlink"
    override val commandData: CommandData =
        Commands.slash(commandName, "Add a link and contribute to Joy's database!")
            .addOption(OptionType.STRING, "group", "Enter the group", true, true)
            .addOption(OptionType.STRING, "idol", "Enter the idol", true, true)
            .addOption(OptionType.STRING, "links", "Enter links followed by their tags", true)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val groupStr = event.getOption("group")!!.asString
        val idol = event.getOption("idol")!!.asString
        val links = event.getOption("links")!!.asString

        val linksWithTags = mutableListOf<MutableList<String>>()
        links.split(" ").map { it.trim() }.forEach { linkOrTag ->
            val link = processLink(linkOrTag)
            if (link != null) {
                linksWithTags.add(mutableListOf(link))
            } else {
                linksWithTags.lastOrNull()?.add(linkOrTag)
            }
        }

        var embedBuilder: EmbedBuilder? = null
        var addedLinks = 0
        var skippedLinks = 0
        val ignoredTags = mutableListOf<String>()
        transaction(DB.getConnection()) {
            fun processTags(linkWithTag: Pair<Link, List<String>>) {
                linkWithTag.second.forEach { tagStr ->
                    val tag = Tag.find { Tags.tagName eq tagStr }
                    if (tag.empty()) {
                        ignoredTags.add(tagStr)
                        return@forEach
                    }
                    LinkTags.insert {
                        it[linkId] = linkWithTag.first.id
                        it[tagId] = tag.first().id
                    }
                }
            }

            fun getOrCreateUser(userId: Long): Long {
                val user = Users.insertIgnoreAndGetId {
                    it[id] = userId
                }

                // These should never be null we just added them. If they are jump.
                User.findById(user!!)!!.contribution++
                return userId
            }

            fun processLink(linkStr: String): Link {
                val link = Link.find { Links.link eq linkStr}
                return if (!link.empty()) {
                    skippedLinks++
                    link.first()
                } else {
                    addedLinks++
                    Link.new {
                        this.link = linkStr
                        this.addedBy = getOrCreateUser(event.user.idLong)
                    }
                }
            }

            val linkObjectsWithTags = linksWithTags.map { linkWithTags ->
                processLink(linkWithTags.removeFirst()) to linkWithTags
            }
            linkObjectsWithTags.forEach(::processTags)

            val group = Group.find { Groups.romanName eq groupStr }
            if (group.empty()) {
                embedBuilder = Theme.errorEmbed("$groupStr does not exist!")
                rollback()
                return@transaction
            }
            val member = Member.find { (Members.romanStageName eq idol) and (Members.id eq group.first().id) }
            if (member.empty()) {
                embedBuilder = Theme.errorEmbed("$idol is not in $groupStr!")
                rollback()
                return@transaction
            }

            linkObjectsWithTags.forEach {
                val l = it.first
                LinkMembers.insertIgnore { statement ->
                    statement[linkId] = l.id
                    statement[memberId] = member.first().id
                }
            }
        }

        if (embedBuilder == null) {
            embedBuilder = EmbedBuilder().apply {
                setTitle("Results:")
                setColor(Theme.BLURPLE)
            }

            if (addedLinks > 0) {
                embedBuilder!!.addField(
                    "Added $addedLinks link${if (addedLinks == 1) "" else "s"} to $groupStr's $idol!",
                    "",
                    false
                )
            }
            if (skippedLinks > 0) {
                embedBuilder!!.addField(
                    "Skipped adding $skippedLinks duplicate link${if (skippedLinks == 1) "" else "s"}.",
                    "",
                    false
                )
            }
            if (ignoredTags.isNotEmpty()) {
                embedBuilder!!.addField(
                    "The following tags are invalid: {${ignoredTags.joinToString(", ")}}.",
                    "",
                    false
                )
            }
        }

        event.replyEmbeds(embedBuilder!!.build()).queue()
    }

    private val supportedExtensions = listOf(
        ".jpg",
        ".jpeg",
        ".png",
    )

    private fun processLink(possibleLink: String): String? {
        val pL = possibleLink.trimEnd('/')
        return if (pL.startsWith("https://gfycat.com/")) {
            pL.replaceAfter("-", "").trimEnd('-')
        }
        else if (pL.startsWith("https://www.redgifs.com/") || pL.startsWith("https://redgifs.com/")) {
            "https://www.redgifs.com/watch/" + pL.substringAfterLast("/")
        }
        else if (pL.startsWith("https://www.gifdeliverynetwork.com/") ||
                 pL.startsWith("https://www.youtu") ||
                 supportedExtensions.any { pL.endsWith(it) }) {
            pL
        } else if (pL.contains("twimg")) {
          "https://pbs.twimg.com/media/" + pL.substringAfterLast("/").substringBeforeLast("?").let {
              it + if (it.endsWith("png")) "?format=png&name=orig" else "?format=jpg&name=orig"
          }
        }
        else {
            null
        }
    }
}