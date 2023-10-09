package com.wanony.joy.discord

import com.wanony.joy.data.dao.DB
import com.wanony.joy.discord.command.auditing.AuditingCommand
import com.wanony.joy.discord.command.autocomplete.GroupAutocompleteProvider
import com.wanony.joy.discord.command.autocomplete.MemberAutocompleteProvider
import com.wanony.joy.discord.command.autocomplete.MemeAutocompleteProvider
import com.wanony.joy.discord.command.autocomplete.TagAutocompleteProvider
import com.wanony.joy.discord.command.gifs.AddLinkCommand
import com.wanony.joy.discord.command.gifs.GifCommand
import com.wanony.joy.discord.command.gifs.RandomLinkCommand
import com.wanony.joy.discord.command.gifs.TimerCommand
import com.wanony.joy.discord.command.manage.*
import com.wanony.joy.discord.command.memes.MemeCommand
import com.wanony.joy.discord.command.misc.AvatarCommand
import com.wanony.joy.discord.command.misc.ClaimPremiumCommand
import com.wanony.joy.discord.command.misc.InformationCommand
import com.wanony.joy.discord.command.misc.SuggestCommand
import com.wanony.joy.discord.command.reddit.RedditCommand
import com.wanony.joy.data.dao.User
import com.wanony.joy.data.dao.Users
import com.wanony.joy.discord.command.AutocompleteProvider
import com.wanony.joy.discord.command.CustomButton
import com.wanony.joy.discord.command.JoyCommand
import dev.minn.jda.ktx.events.listener
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.internal.utils.PermissionUtil
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import java.util.*

class JoyBot(
    val jda: JDA,
    commands: Map<String, JoyCommand>,
    private val autoCompleteProviders: List<AutocompleteProvider>,
    private val buttons: Map<String, CustomButton>
) {

    val commands: Map<String, JoyCommand>

    init {
        this.commands = commands.filter {
            it.value.setup().also { success ->
                if (!success) println("Disabling command '${it.value.commandName}' - setup returned false.")
            }
        }
        updateCommands()
    }

    suspend fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commands[event.name]?.let {
            it.execute(event)
            return
        }

        // No command was found
        event.reply("I can't handle that command right now.").setEphemeral(true).queue()
    }

    fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        autoCompleteProviders.firstOrNull {
            it.autoComplete(event)
        }
    }

    suspend fun onButtonInteraction(event: ButtonInteractionEvent) {
        buttons[event.button.id]?.let {
            it.execute(event)
            return
        }
    }

    suspend fun onRoleAdded(event: GuildMemberRoleAddEvent) = DB.transaction {
        // Check if the added role is the one you are interested in.
//        val roleId = "1159465237561475142" // Replace with the actual role ID you want to track.
        val roleId = "1153262194562183178"
        if (event.roles.any { it.id == roleId }) {
            val userId = event.user.idLong
            val user = Users.insertIgnoreAndGetId { it[id] = userId }
            User.findById(user!!)!!.isPremium = true
            return@transaction
        }
    }

    suspend fun onRoleRemoved(event: GuildMemberRoleRemoveEvent) = DB.transaction {
        // Check if the added role is the one you are interested in.
//        val roleId = "1159465237561475142" // Replace with the actual role ID you want to track.
        val roleId = 1153262194562183178
        if (event.roles.any { it.idLong == roleId }) {
            val userId = event.user.idLong
            val user = Users.insertIgnoreAndGetId { it[id] = userId }
            User.findById(user!!)!!.isPremium = false
            return@transaction
        }
    }


    private fun updateCommands() {
        listOfNotNull(
            jda.updateCommands(),
            jda.getGuildById(getProperty<String>("testGuild"))?.updateCommands()
        ).forEach { commandUpdateAction ->
            commands.values.forEach {
                commandUpdateAction.addCommands(
                    when(it.commandName) {
                        // TODO need to update this to work with Joys DB rather than discord perms
                        "admin" -> it.commandData.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        "manage" -> it.commandData.setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                        "guild" -> it.commandData.setGuildOnly(true)
                        "claim" -> it.commandData.setGuildOnly(true)
                        else -> it.commandData
                    }
                )
            }
            commandUpdateAction.queue()
        }
    }

}

fun SlashCommandInteractionEvent.checkGuildReplyPermissions() : Boolean {
    val replyChannel = (channel as? GuildMessageChannel) ?: return false
    val joy = guild?.getMember(jda.selfUser) ?: return false
    return !PermissionUtil.checkPermission(replyChannel.permissionContainer, joy, Permission.MESSAGE_SEND)
}



fun main() {
    val token = getProperty<String>("discordAPIToken")

    val allAutocompleteProviders : List<AutocompleteProvider> = listOf(
        GroupAutocompleteProvider(),
        MemberAutocompleteProvider(),
        TagAutocompleteProvider(),
        MemeAutocompleteProvider(),
    )

    val allButtons : Map<String, CustomButton> = listOf(
        DeleteButton(),
        KeepDataButton(),
    ).associateBy { it.buttonId }

    val jda = light(token, enableCoroutines = true) {
//        intents += listOf(GatewayIntent.GUILD_MEMBERS)
    }

    val allCommands : Map<String, JoyCommand> = listOf(
        AvatarCommand(),
        SuggestCommand(),
        AddLinkCommand(jda),
        RandomLinkCommand(),
        ManageCommand(),
        GifCommand(),
        InformationCommand(),
        TimerCommand(),
        MemeCommand(),
        RedditCommand(jda),
//        InstagramCommand(jda), // Error on logging in
        AuditingCommand(jda),
        DataCommand(),
        ManageGuildCommand(),
        AdminCommand(jda),
        ClaimPremiumCommand()
    ).associateBy { it.commandName }

    val joy = JoyBot(jda, allCommands, allAutocompleteProviders, allButtons)
    println("setup is finished boss")
    jda.listener<SlashCommandInteractionEvent> { event ->
        joy.onSlashCommandInteraction(event)
    }
    jda.listener<CommandAutoCompleteInteractionEvent> { event ->
        joy.onCommandAutoCompleteInteraction(event)
    }
    jda.listener<ButtonInteractionEvent> { event ->
        joy.onButtonInteraction(event)
    }
//    jda.listener<GuildMemberRoleAddEvent> { event ->
//        joy.onRoleAdded(event)
//    }
//    jda.listener<GuildMemberRoleRemoveEvent> { event ->
//        joy.onRoleRemoved(event)
//    }
    println("listeners are listening boss")

}

/**
 * Attempts to find the property given by [key] within misc.properties.
 *
 * @return the value of the property as [T] or null if not found.
 */
@Suppress("UNCHECKED_CAST")
fun <T> findProperty(key: String): T? {
    val props = ClassLoader.getSystemClassLoader().getResourceAsStream("misc.properties").use {
        it ?: throw java.lang.RuntimeException("Missing properties file misc.properties")
        Properties().apply { load(it) }
    }
    return (props.getProperty(key) as T)

}

/**
 * Attempts to get the property given by [key] within misc.properties.
 *
 * Throws a runtime exception is the property does not exist.
 *
 * @return the value of the property
 */
fun <T> getProperty(key: String): T =
    findProperty<T>(key) ?: throw java.lang.RuntimeException("Could not find property with key '$key' in misc.properties")

