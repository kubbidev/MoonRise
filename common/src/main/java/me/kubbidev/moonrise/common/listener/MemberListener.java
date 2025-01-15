package me.kubbidev.moonrise.common.listener;

import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.model.ApiMember;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class MemberListener extends ListenerAdapter {
    private final GatewayClient client;

    public MemberListener(GatewayClient client) {
        this.client = client;
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

        this.client.modifyMember(member, action).exceptionally(t -> {
            this.client.getPlugin().getLogger().warn(errorMessage, t);
            return null;
        });
    }
}
