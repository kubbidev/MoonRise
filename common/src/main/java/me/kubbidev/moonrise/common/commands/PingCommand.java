package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        context.setDeferred(true);
        context.sendMessage(Component.text("Pong!"));
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("ping", "Pong!");
    }
}
