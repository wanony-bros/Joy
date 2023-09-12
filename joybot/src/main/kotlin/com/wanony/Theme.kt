package com.wanony

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.Channel
import java.awt.Color

class Theme {
    companion object {
        val BLURPLE = Color(114, 137, 218)
        fun errorEmbed(message: String) : EmbedBuilder = EmbedBuilder().apply {
            setTitle("Error!")
            setDescription(message)
            setColor(Color.RED)
        }

        fun successEmbed(message: String) : EmbedBuilder = EmbedBuilder().apply {
            setTitle("Success!")
            setDescription(message)
            setColor(Color.GREEN)
        }

        fun genericErrorEmbed() = errorEmbed("Something went wrong!").build()

        fun Channel.toLink(): String = "<#${id}>"
    }
}