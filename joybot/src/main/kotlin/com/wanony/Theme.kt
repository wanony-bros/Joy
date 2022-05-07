package com.wanony

import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class Theme {
    companion object {
        val BLURPLE = Color(114, 137, 218)
        fun errorEmbed(message: String) = EmbedBuilder().apply {
            setTitle("Error!")
            setDescription(message)
            setColor(Color.RED)
        }

        fun successEmbed(message: String) = EmbedBuilder().apply {
            setTitle("Success!")
            setDescription(message)
            setColor(Color.GREEN)
        }
    }
}