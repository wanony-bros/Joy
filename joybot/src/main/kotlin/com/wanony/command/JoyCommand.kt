package com.wanony.command

import com.wanony.command.autocomplete.GroupAutocompleteProvider
import com.wanony.command.autocomplete.MemberAutocompleteProvider
import com.wanony.command.autocomplete.TagAutocompleteProvider
import com.wanony.command.gfys.AddLinkCommand
import com.wanony.command.gfys.GfyCommand
import com.wanony.command.gfys.RandomLinkCommand
import com.wanony.command.manage.ManageCommand
import com.wanony.command.misc.AvatarCommand
import com.wanony.command.misc.SuggestCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

val allCommands : Map<String, JoyCommand> = listOf(
    AvatarCommand(),
    SuggestCommand(),
    AddLinkCommand(),
    RandomLinkCommand(),
    ManageCommand(),
    GfyCommand(),
).associateBy { it.commandName }

val allAutocompleteProviders : List<AutocompleteProvider> = listOf(
    GroupAutocompleteProvider(),
    MemberAutocompleteProvider(),
    TagAutocompleteProvider(),
)

interface JoyCommand {
    val commandName: String
    val commandData: CommandData

    fun execute(event: SlashCommandInteractionEvent)
}