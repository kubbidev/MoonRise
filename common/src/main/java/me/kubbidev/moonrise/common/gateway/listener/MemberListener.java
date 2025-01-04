package me.kubbidev.moonrise.common.gateway.listener;

import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class MemberListener extends ListenerAdapter {
    private final MoonRisePlugin plugin;

    public MemberListener(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isBot(Member member) {
        return member.getUser().isBot();
    }

    @Override
    public void onGuildMemberUpdateNickname(@NotNull GuildMemberUpdateNicknameEvent e) {
        this.handleMemberUpdate(e.getMember(), e.getOldNickname(), e.getNewNickname(), m -> m.setNickname(e.getNewNickname()),
                "An error occurred while updating member nickname");
    }

    @Override
    public void onGuildMemberUpdateAvatar(@NotNull GuildMemberUpdateAvatarEvent e) {
        this.handleMemberUpdate(e.getMember(), e.getOldAvatarUrl(), e.getNewAvatarUrl(), m -> m.setGuildAvatar(e.getNewAvatarUrl()),
                "An error occurred while updating member avatar");
    }

    /**
     * A generic method to handle member updates with minimal duplication.
     */
    private <T> void handleMemberUpdate(Member member, T oldValue, T newValue, Consumer<ApiMember> action, String errorMessage) {
        if (Objects.equals(oldValue, newValue)) return;
        if (isBot(member)) return;

        this.plugin.getGatewayClient().modifyMember(member, action)
                .exceptionally(t -> {
                    this.plugin.getLogger().warn(errorMessage, t);
                    return null;
                });
    }
}
