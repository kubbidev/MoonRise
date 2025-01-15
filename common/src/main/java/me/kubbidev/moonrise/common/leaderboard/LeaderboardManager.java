package me.kubbidev.moonrise.common.leaderboard;

import me.kubbidev.moonrise.common.GatewayClient;
import me.kubbidev.moonrise.common.message.source.MessageChannelSource;
import me.kubbidev.moonrise.common.model.ApiGuild;
import me.kubbidev.moonrise.common.model.ApiMember;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerTask;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class LeaderboardManager implements Runnable, AutoCloseable {
    /** A constant representing the UTC timezone as used */
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");

    /**
     * A constant representing the specific day of the week on which the scheduled
     * operations of the {@link LeaderboardManager} are intended to occur.
     */
    public static final DayOfWeek DAY_OF_WEEK = DayOfWeek.MONDAY;

    /**
     * A constant representing the specific time of day at which the
     * {@link LeaderboardManager} operations are scheduled to occur.
     *
     * <p>This value defines the daily fixed time set to 18:00 (6:00 PM).</p>
     */
    public static final LocalTime TIME_OF_DAY = LocalTime.of(18, 0);


    private final GatewayClient client;
    private boolean scheduled;

    @Nullable
    private SchedulerTask repeatingTask = null;

    public LeaderboardManager(GatewayClient client) {
        this.client = client;
    }

    public boolean isScheduled() {
        return this.scheduled;
    }

    @Override
    public void close() {
        if (this.repeatingTask != null) {
            this.repeatingTask.cancel();
        }
        this.scheduled = false;
    }

    public void schedule() {
        if (isScheduled()) {
            throw new IllegalStateException("Already scheduled");
        }

        SchedulerAdapter adapter = this.client.getPlugin().getBootstrap().getScheduler();
        this.scheduled = true;
        this.repeatingTask = adapter.asyncLater(() -> {
            // Run the task manually for the first time, if we don't do this the
            // execution will be at the next schedule date
            this.run();

            // And now that we are the right date time, normally schedule the repeating task
            this.repeatingTask = adapter.asyncRepeating(this, 7, TimeUnit.DAYS);
        }, calculateInitialMillisDelay(), TimeUnit.MILLISECONDS);
    }

    private long calculateInitialMillisDelay() {
        return Duration.between(getCurrentDate(), getNextScheduleDate()).toMillis();
    }

    private ZonedDateTime getCurrentDate() {
        return ZonedDateTime.now(ZONE_ID);
    }

    public ZonedDateTime getNextScheduleDate() {
        ZonedDateTime currentDate = getCurrentDate();
        ZonedDateTime targetDateTime = currentDate.with(TIME_OF_DAY);

        return currentDate.isBefore(targetDateTime)
                ? targetDateTime.with(
                TemporalAdjusters.nextOrSame(DAY_OF_WEEK))

                : targetDateTime.with(
                TemporalAdjusters.next(DAY_OF_WEEK));
    }

    @Override
    public void run() {
        ShardManager shardManager = this.client.getShardManager().orElseThrow(
                () -> new IllegalStateException("Shard manager seems to be null?"));

        // Retrieve the instance of the plugin linked to the client
        MoonRisePlugin plugin = this.client.getPlugin();
        this.client.getGuilds().thenComposeAsync(g -> {
            List<CompletableFuture<?>> futures = new ArrayList<>(g.size());
            g.forEach(apiGuild -> {
                if (!apiGuild.isLeaderboardEnabled()) return;

                // Retrieve the corresponding guild from its id
                var guildById = shardManager.getGuildById(apiGuild.getId());
                if (guildById != null) {
                    futures.add(renderLeaderboard(guildById, apiGuild));
                }
            });
            return CompletableFutures.allOf(futures);
        }).exceptionally(t -> {
            plugin.getLogger().warn("Exception occurred while rendering guilds leaderboard", t);
            return null; // Needed to comply with the signature of exceptionally
        });
    }

    public CompletableFuture<Void> renderLeaderboard(Guild guild, ApiGuild apiGuild) {
        var channel = getLeaderboardChannel(guild, apiGuild);
        if (channel == null) return CompletableFutures.NULL;

        return getSortedMembers(guild).thenComposeAsync(apiMembers -> {
            if (apiMembers.isEmpty()) {
                return CompletableFutures.NULL;
            }

            Leaderboard leaderboard = new Leaderboard(this);
            CompletableFuture<?> future1 = writeMembers(apiMembers, leaderboard);
            CompletableFuture<?> future2 = sendLeaderboard(channel, leaderboard);

            return CompletableFuture.allOf(future1, future2);
        });
    }

    private @Nullable GuildMessageChannel getLeaderboardChannel(Guild guild, ApiGuild apiGuild) {
        return guild.getChannelById(GuildMessageChannel.class, apiGuild.getLeaderboardChannelId());
    }

    private CompletableFuture<List<ApiMember>> getSortedMembers(Guild guild) {
        return this.client.getMembersWithHighestExperience(guild, Leaderboard.MAX_ENTRIES);
    }

    private CompletableFuture<Void> writeMembers(List<ApiMember> apiMembers, Leaderboard leaderboard) {
        List<CompletableFuture<?>> futures = new ArrayList<>(apiMembers.size());

        for (int i = 0; i < apiMembers.size(); i++) {
            ApiMember apiMember = apiMembers.get(i);

            int currentPlacement = (i + 1);
            int previousPlacement = apiMember.getPlacement();
            if (previousPlacement < 0) {
                previousPlacement = Leaderboard.MAX_ENTRIES + 1;
            }

            leaderboard.registerEntry(new Leaderboard.Entry(
                    apiMember.getDisplayName(),
                    apiMember.getExperience(),
                    currentPlacement,
                    previousPlacement
            ));

            if (previousPlacement != currentPlacement) {
                apiMember.setPlacement(currentPlacement);

                futures.add(this.client.saveMember(apiMember));
            }
        }

        return CompletableFutures.allOf(futures);
    }

    private CompletableFuture<Message> sendLeaderboard(GuildMessageChannel channel, Leaderboard leaderboard) {
        return channel.getIterableHistory().takeAsync(10).thenAcceptAsync(messages -> channel.purgeMessages(messages.stream()
                .filter(message -> !message.isPinned())
                .filter(message -> !message.isEphemeral()).collect(ImmutableCollectors.toList()
                ))
        ).thenComposeAsync(__ -> MessageChannelSource.wrap(channel).sendMessage(leaderboard.build())
                .thenComposeAsync(message -> {
                    if (channel instanceof NewsChannel) {
                        return ((NewsChannel) channel).crosspostMessageById(message.getId()).submit();
                    } else {
                        return CompletableFuture.completedFuture(message);
                    }
                }));
    }
}
