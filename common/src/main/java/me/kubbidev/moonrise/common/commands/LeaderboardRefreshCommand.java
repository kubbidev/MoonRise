package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LeaderboardRefreshCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        Objects.requireNonNull(context.getGuild());
        plugin.getGatewayClient().getGuild(context.getGuild()).thenComposeAsync(apiGuild -> {
            if (!apiGuild.isLeaderboardEnabled()) {
                context.setDeferred(true);
                context.sendMessage(Message.LEADERBOARD_NOT_ACTIVE.build());
                return CompletableFutures.NULL;
            }

            return plugin.getGatewayClient().getLeaderboard().renderLeaderboard(context.getGuild(), apiGuild)
                .thenAcceptAsync(__ -> {
                    context.setDeferred(true);
                    context.sendMessage(Message.LEADERBOARD_REFRESHED.build());
                });
        }).join();
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("leaderboard_refresh", "Broadcast the freshly new updated leaderboard.")
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
