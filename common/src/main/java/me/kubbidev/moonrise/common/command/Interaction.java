package me.kubbidev.moonrise.common.command;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.message.source.Source;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

/**
 * The Interaction interface represents a contract that defines the structure
 * of an executable interaction.
 */
public interface Interaction {

    /**
     * Executes the {@link Interaction} synchronously with the provided plugin, channel, and context.
     * <p>
     * This method should contain the main logic for processing the interaction.
     *
     * @param plugin the instance of the {@link MoonRisePlugin} executing this interaction
     * @param channel the {@link Source} representing the channel where the interaction was initiated
     * @param context the {@link InteractionContext} providing details about the interaction
     */
    void execute(MoonRisePlugin plugin, Source channel, InteractionContext context);

    /**
     * Retrieves the {@link SlashCommandData} associated with the interaction's slash command.
     * <p>
     * This method is used to provide metadata about the command, such as its name, description,
     * and options, for registration with Discord's slash command system.
     *
     * @return a non-null instance of {@link SlashCommandData} representing the metadata for the command
     */
    @NotNull SlashCommandData getMetadata();
}