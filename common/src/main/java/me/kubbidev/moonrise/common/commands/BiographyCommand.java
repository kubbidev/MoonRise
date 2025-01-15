package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BiographyCommand implements Interaction {
    private static final int BIOGRAPHY_MAX_LENGTH = 300;

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        Objects.requireNonNull(context.getMember());

        var message = context.get("message", OptionMapping::getAsString);
        if (message != null) {
            message = message.replace("\\n", " ");

            if (message.length() > BIOGRAPHY_MAX_LENGTH) {
                message = message.substring(0, BIOGRAPHY_MAX_LENGTH - 3) + "...";
            }
        }

        String biography = message;
        plugin.getGatewayClient().modifyMember(context.getMember(), m -> m.setBiography(biography))
                .thenAcceptAsync(__ -> {
                    context.setDeferred(true);
                    context.sendMessage(Message.BIOGRAPHY_UPDATED.build());
                }).join();
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("biography", "Display a pretty description in your profile (empty execution will reset your bio).")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "message", "The biography to display.");
    }
}
