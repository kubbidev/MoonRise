package me.kubbidev.moonrise.common.leaderboard.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.util.CaffeineFactory;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSuppressEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ActivityListener extends ActivityService {

    private final GatewayClient                     client;
    private final Cache<Long, GuildActivityTracker> activityTrackers = CaffeineFactory.newBuilder()
        .expireAfterAccess(Duration.ofDays(10)).build();

    public ActivityListener(GatewayClient client) {
        super(client);
        this.client = client;
    }

    private boolean isBot(Member member) {
        return member.getUser().isBot();
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent e) {
        AudioChannel newChannel = e.getChannelJoined();
        AudioChannel oldChannel = e.getChannelLeft();
        if (isBot(e.getMember())) {
            return;
        }

        this.client.getGuild(e.getGuild()).thenComposeAsync(apiGuild -> {
            if (!apiGuild.isLeaderboardEnabled()) {
                return CompletableFutures.NULL;
            }

            List<CompletableFuture<?>> futures = new ArrayList<>();
            if (oldChannel != null && isChannelAllowed(oldChannel)) {
                futures.add(stopRecordingActivity(e));
            }

            if (newChannel != null && isChannelAllowed(newChannel)) {
                futures.add(startRecordingActivity(e));
            }
            return CompletableFutures.allOf(futures);
        }).exceptionally(t -> {
            this.client.getPlugin().getLogger().warn("An error occurred while updating activity", t);
            return null;
        });
    }

    @Override
    public void onGuildVoiceMute(@NotNull GuildVoiceMuteEvent e) {
        this.handleGuildFreezing(e);
    }

    @Override
    public void onGuildVoiceSuppress(@NotNull GuildVoiceSuppressEvent e) {
        this.handleGuildFreezing(e);
    }

    private CompletableFuture<Void> stopRecordingActivity(GuildVoiceUpdateEvent e) {
        AudioChannel oldChannel = Objects.requireNonNull(e.getChannelLeft());
        long guildId = oldChannel.getGuild().getIdLong();

        var tracker = this.activityTrackers.getIfPresent(guildId);
        if (tracker == null) {
            return CompletableFutures.NULL;
        }

        var state = tracker.removeChannel(
            oldChannel.getIdLong(), e.getMember());

        if (tracker.isEmpty()) {
            this.activityTrackers.invalidate(guildId);
        }
        return state != null ? super.updateActivity(e, state) : CompletableFutures.NULL;
    }

    private CompletableFuture<Void> startRecordingActivity(GuildVoiceUpdateEvent e) {
        AudioChannel newChannel = Objects.requireNonNull(e.getChannelJoined());
        long guildId = newChannel.getGuild().getIdLong();

        var tracker = this.activityTrackers.get(guildId, __ -> new GuildActivityTracker(this.client.getPlugin()));
        if (tracker != null) {
            tracker.addChannel(newChannel.getIdLong(), e.getMember().getIdLong(), isFrozen(e.getMember()));
        }
        return CompletableFutures.NULL;
    }

    private void handleGuildFreezing(GenericGuildVoiceEvent e) {
        if (!isBot(e.getMember())) {
            updateFreeze(e.getMember(), e.getVoiceState());
        }
    }

    private void updateFreeze(Member member, GuildVoiceState voiceState) {
        var tracker = this.activityTrackers.getIfPresent(member.getGuild().getIdLong());
        if (tracker != null) {
            tracker.freeze(member.getIdLong(), isFrozen(voiceState));
        }
    }

    private static boolean isChannelAllowed(AudioChannel channel) {
        return !channel.equals(channel.getGuild().getAfkChannel());
    }

    private static boolean isFrozen(Member member) {
        return member.getVoiceState() != null && isFrozen(member.getVoiceState());
    }

    private static boolean isFrozen(GuildVoiceState voiceState) {
        return !voiceState.inAudioChannel() || voiceState.isMuted() || voiceState.isSuppressed();
    }
}
