package com.wanony.command.gfys

import com.wanony.DB
import com.wanony.Theme.Companion.errorEmbed
import com.wanony.command.JoyBotCommand
import com.wanony.dao.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.jetbrains.exposed.sql.Random
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class RandomLinkCommand : JoyBotCommand {
    private val recentlyAdded = LinkProvider()
    override val name: String = "random"
    override val commandData: CommandData =
        Commands.slash(name, "Get a random link from Joy's database!")

    private fun getRandomLink(): ResultRow? {
        return transaction(DB.getConnection()) {
                Links.innerJoin(LinkMembers).innerJoin(Members).innerJoin(Groups)
                    .selectAll().orderBy(Random()).limit(1).firstOrNull()
        }
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val embedBuilder: EmbedBuilder?
        val channel = event.channel.idLong
        var randomLink: ResultRow?
        while (true) {
            randomLink = getRandomLink()
            if (randomLink == null) {
                embedBuilder = errorEmbed("No links added to Joy, add some with `/addlink`!")
                event.replyEmbeds(embedBuilder.build())
                return
            }
            val link = randomLink[Links.link]
            if (!this.recentlyAdded.contains(channel, link) or (!this.recentlyAdded.isFull(channel))) {
                // if it sends 100 links and then finds a duplicate it should be fine
                // probably worth checking in the future, but probably will not error in production
                break
            }
        }
        val link = randomLink!![Links.link]
        val group = randomLink[Groups.romanName]
        val member = randomLink[Members.romanName]
        this.recentlyAdded.addToRecent(channel, link)
        event.reply("Random choice! `${group}`'s `${member}`!\n${link}").queue()
    }
}