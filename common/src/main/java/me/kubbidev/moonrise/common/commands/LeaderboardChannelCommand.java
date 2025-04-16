package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.Interaction;
import me.kubbidev.moonrise.common.command.InteractionContext;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LeaderboardChannelCommand implements Interaction {

    @Override
    public void execute(MoonRisePlugin plugin, Source channel, InteractionContext context) {
        Objects.requireNonNull(context.getGuild());

        GuildChannelUnion selected = context.get("channel", OptionMapping::getAsChannel);
        assert selected != null;

        plugin.getGatewayClient().modifyGuild(context.getGuild(), g -> g.setLeaderboardChannelId(selected.getIdLong()))
            .thenAcceptAsync(__ -> {
                context.setDeferred(true);
                context.sendMessage(Message.LEADERBOARD_CHANNEL_UPDATED.build(selected));
            }).join();
    }

    @Override
    public @NotNull SlashCommandData getMetadata() {
        return Commands.slash("leaderboard_channel", "Configure the channel were the leaderboard will be broadcast.")
            .setGuildOnly(true)
            .addOption(OptionType.CHANNEL, "channel", "The channel to broadcast.", true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER));
    }
}
