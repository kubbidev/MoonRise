package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LeaderboardEnabledCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        Objects.requireNonNull(context.getGuild());

        Boolean action = context.get("action", OptionMapping::getAsBoolean);
        assert action != null;

        plugin.getGatewayClient().modifyGuild(context.getGuild(), g -> g.setLeaderboardEnabled(action))
                .thenAcceptAsync(__ -> {
                    context.setDeferred(true);
                    context.sendMessage((action
                            ? Message.LEADERBOARD_ENABLE
                            : Message.LEADERBOARD_DISABLE).build());
                }).join();
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("leaderboard_enabled", "Controls the servers ranking and experience system.")
                .setGuildOnly(true)
                .addOption(OptionType.BOOLEAN, "action", "Whether to enable/disable the system.", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
