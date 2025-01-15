package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.message.ComponentEmbed;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.DurationFormatter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;

public class AboutCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        ShardManager shardManager = plugin.getGatewayClient().getShardManager().orElseThrow(
                () -> new IllegalStateException("Cannot get ShardManager"));

        ComponentEmbed componentEmbed = new ComponentEmbed();
        componentEmbed.author(Component.translatable("moonrise.command.about.statistic-key"));
        componentEmbed.footer(Component.translatable("moonrise.command.misc.requested",
                Component.text(context.getUser().getEffectiveName())
        ), context.getUser().getEffectiveAvatarUrl());

        componentEmbed.timestamp(OffsetDateTime.now());
        componentEmbed.color(0x1663FF);
        componentEmbed.field(
                Component.translatable("moonrise.command.about.total-key"),
                Component.text()
                        .append(Component.text("```yaml"))
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.total.guilds-key")
                                .append(Component.text(": "))
                                .append(Component.text(shardManager.getGuildCache().size()))
                        )
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.total.users-key")
                                .append(Component.text(": "))
                                .append(Component.text(shardManager.getUserCache().size()))
                        )
                        .append(Component.newline())
                        .append(Component.text("```"))
                        .build(), false);

        componentEmbed.field(
                Component.translatable("moonrise.command.about.metadata-key"),
                Component.text()
                        .append(Component.text("```yaml"))
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.metadata.version-key")
                                .append(Component.text(": "))
                                .append(Component.text(plugin.getBootstrap().getVersion()))
                        )
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.metadata.platform-key")
                                .append(Component.text(": "))
                                .append(Component.text(plugin.getBootstrap().getType().getFriendlyName()))
                        )
                        .append(Component.newline())
                        .append(Component.text("```"))
                        .build(), false);

        componentEmbed.field(
                Component.translatable("moonrise.command.about.other-key"),
                Component.text()
                        .append(Component.text("```yaml"))
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.other.java-key")
                                .append(Component.text(": "))
                                .append(Component.text(System.getProperty("java.vendor.version")))
                        )
                        .append(Component.newline())
                        .append(Component.newline())
                        .append(Component.translatable()
                                .key("moonrise.command.about.other.uptime-key")
                                .append(Component.text(": "))
                                .append(DurationFormatter.CONCISE_LOW_ACCURACY.format(plugin.getBootstrap().getStartupDuration()))
                        )
                        .append(Component.newline())
                        .append(Component.text("```"))
                        .build(), false);

        context.setDeferred(false);
        context.sendMessage(componentEmbed);
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("about", "Prints general information about the active bot instance.");
    }
}
