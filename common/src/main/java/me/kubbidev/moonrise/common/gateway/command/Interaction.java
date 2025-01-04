package me.kubbidev.moonrise.common.gateway.command;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.gateway.message.source.Source;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 * The Interaction interface represents a contract that defines the structure
 * of an executable interaction.
 */
public interface Interaction {

    /**
     * Executes the {@link Interaction} with the provided plugin, channel, and context.
     *
     * @param plugin the instance of the MoonRise plugin executing this interaction
     * @param channel the channel where the interaction is being executed
     * @param context the context of the interaction, containing command and user information
     */
    void execute(MoonRisePlugin plugin, Source channel, InteractionContext context);

    /**
     * Retrieves the {@link SlashCommandData} associated with the slash command.
     *
     * @return A non-null instance of {@link SlashCommandData} representing the metadata for the command.
     */
    @NotNull SlashCommandData getMetadata();
}