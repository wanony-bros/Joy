package com.wanony.command.memes

import com.wanony.DB
import com.wanony.Theme
import com.wanony.command.JoyCommand
import com.wanony.dao.Memes
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class MemeCommand : JoyCommand {
    override val commandName: String = "meme"
    override val commandData: CommandData = Commands.slash(
        commandName, "Meme it out"
    )
        .addOption(OptionType.STRING, "meme", "The meme to get/add to Joy", true)
        .addOption(OptionType.STRING, "content", "Content to be added to this meme", false)

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val meme = event.getOption("meme")!!.asString
        val content = event.getOption("content")?.asString
        val user = event.user.idLong

        val memeContent = getMeme(meme)

        if (memeContent != null) {
            // we got a meme, we send a meme
            event.reply(memeContent).queue()
            return
        }
        if (content != null) {
            // if the user supplied some content, we try to add it
            if (addMeme(meme, content, user)) {
                event.replyEmbeds(Theme.successEmbed("Added meme: $meme!").build()).queue()
                return
            }
            event.replyEmbeds(Theme.errorEmbed("Meme: $meme already exists! Try a new name").build()).queue()
        }
        // no meme, no caption, instruct user to add a caption
        event.replyEmbeds(
            Theme.errorEmbed("To add a meme, please provide a content slash option!").build()).queue()
    }

    // add meme to the database
    private fun addMeme(meme: String, content: String, userId: Long): Boolean = DB.transaction {
        Memes.insert {
            it[Memes.meme] = meme
            it[Memes.content] = content
            it[Memes.addedBy] = userId
        }.insertedCount > 0
    }

    // get meme from the DB
    private fun getMeme(meme: String): String? = DB.transaction {
        Memes.select { Memes.meme eq meme }.firstOrNull()?.let { it[Memes.content] }
    }
}