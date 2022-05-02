package com.wanony.command.misc

import com.wanony.Theme
import com.wanony.command.JoyBotCommand
import com.wanony.getProperty
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import java.awt.Color

class SuggestCommand : JoyBotCommand {
    override val name: String = "suggest"
    override val commandData: CommandData =
        Commands.slash(name, "Suggest a new feature for Joy")
            .addOption(OptionType.STRING, "suggestion", "Provide your suggestion", true)
            .addOption(OptionType.BOOLEAN, "anonymity", "Do you want to suggest anonymously?", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        // This option is required it should always be present
        val suggestion = event.getOption("suggestion")!!.asString
        val isAnonymous = event.getOption("anonymity")?.asBoolean ?: true
        // TODO: To not crash here log that the suggestions channel is broken!
        val suggestionChannel = event.jda.getTextChannelById(getProperty<String>("suggestChannel"))!!

        val embed = EmbedBuilder().apply {
            setTitle("Suggestion")
            setDescription(suggestion)
            setColor(Theme.BLURPLE)

            if (!isAnonymous) {
                setFooter("Suggested by ${event.user.asTag}", event.user.effectiveAvatarUrl)
            }
        }.build()
        suggestionChannel.sendMessageEmbeds(embed).queue()

        val thanksEmbed = EmbedBuilder().apply {
            setTitle("Thanks You!")
            setDescription(if (isAnonymous)
                "Your suggestion has been recorded anonymously and will be looked at as soon as possible!"
            else
                "Your suggestion has been recorded and it will be looked at as soon as possible!"
            )
            setColor(Color.GREEN)
        }.build()
        event.replyEmbeds(thanksEmbed).setEphemeral(true).queue()
    }
}