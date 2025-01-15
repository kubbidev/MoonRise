package me.kubbidev.moonrise.common.leaderboard.tracker;

import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.leaderboard.activity.MemberState;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import me.kubbidev.moonrise.common.util.ExpiringSet;
import me.kubbidev.moonrise.common.util.Long2;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class ActivityService extends ListenerAdapter {
    private final GatewayClient client;

    /** A thread-safe set that holds {@link Long2} identifiers to manage a cooldown period */
    private final Set<Long2> messageCooldowns = ExpiringSet.newExpiringSet(1, TimeUnit.MINUTES);

    /** An instance of {@link Random} used for generating pseudo-random numbers throughout */
    private final Random random = new Random();

    public ActivityService(GatewayClient client) {
        this.client = client;
    }

    protected int getExperienceMultiplier() {
        return this.client.getPlugin().getConfiguration().get(ConfigKeys.ACTIVITY_EXPERIENCE_MULTIPLIER);
    }

    protected CompletableFuture<Void> updateActivity(GuildVoiceUpdateEvent e, MemberState state) {
        return this.client.modifyMember(e.getMember(), apiMember -> {
            long f = (long) (state.getActivityPoints().get() * getExperienceMultiplier() * 15);
            long g = apiMember.getExperience() + f;

            long h = state.getActivityTime().get();
            long i = apiMember.getVoiceActivity() + h;

            apiMember.setExperience(g);
            apiMember.setVoiceActivity(i);
        });
    }

    protected void updateMessageActivity(MessageReceivedEvent e) {
        this.client.getGuild(e.getGuild()).thenComposeAsync(apiGuild -> {
            if (!apiGuild.isLeaderboardEnabled()) return CompletableFutures.NULL;

            var member = e.getMember();
            if (member == null) return CompletableFutures.NULL;

            return this.client.modifyMember(member, apiMember -> {
                long f = this.random.nextLong(15L, 25L) * getExperienceMultiplier();
                long g = apiMember.getExperience() + f;

                apiMember.setExperience(g);
            });
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        if (e.isWebhookMessage() || !e.isFromGuild() || e.getAuthor().isBot()) return;

        Long2 identifier = new Long2(
                e.getAuthor().getIdLong(),
                e.getGuild().getIdLong());

        if (!this.messageCooldowns.contains(identifier)) {
            updateMessageActivity(e);
            this.messageCooldowns.add(identifier);
        }
    }
}
