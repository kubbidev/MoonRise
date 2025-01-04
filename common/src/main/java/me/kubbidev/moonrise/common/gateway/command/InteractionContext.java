package me.kubbidev.moonrise.common.gateway.command;

import me.kubbidev.moonrise.common.gateway.message.source.InteractionSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Event class used to parse and provide Information about a command execution.
 */
public class InteractionContext extends InteractionSource {

    /**
     * Constructs an {@link InteractionContext} that provides context for a command interaction.
     *
     * @param interaction the {@link CommandInteraction} instance associated with this context
     */
    protected InteractionContext(CommandInteraction interaction) {
        super(interaction);
    }

    public @NotNull User getUser() {
        return this.interaction.getUser();
    }

    public @Nullable Guild getGuild() {
        return this.interaction.getGuild();
    }

    public @Nullable Member getMember() {
        return this.interaction.getMember();
    }

    public @NotNull MessageChannel getChannel() {
        return this.interaction.getMessageChannel();
    }

    public @Nullable <T> T get(String arg, Function<OptionMapping, T> mapping) {
        return this.interaction.getOption(arg, mapping);
    }
}