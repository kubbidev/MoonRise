package me.kubbidev.moonrise.common;

import me.kubbidev.moonrise.common.command.InteractionManager;
import me.kubbidev.moonrise.common.leaderboard.LeaderboardManager;
import me.kubbidev.moonrise.common.leaderboard.tracker.ActivityListener;
import me.kubbidev.moonrise.common.listener.GuildListener;
import me.kubbidev.moonrise.common.listener.MemberListener;
import me.kubbidev.moonrise.common.listener.UserListener;
import me.kubbidev.moonrise.common.plugin.AbstractMoonRisePlugin;
import me.kubbidev.moonrise.common.retriever.AbstractEntityRetriever;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.storage.Storage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.*;

public class GatewayClient extends AbstractEntityRetriever implements AutoCloseable {

    private final     MoonRisePlugin     plugin;
    /**
     * A synchronization aid used to manage the shutdown process of asynchronous
     */
    private final     CountDownLatch     shutdownLatch = new CountDownLatch(1);
    /**
     * Manages and handles user interactions within the system
     */
    private final     InteractionManager interactionManager;
    private final     LeaderboardManager leaderboardManager;
    /**
     * Manages the lifecycle and functionality of shards for a distributed client gateway
     */
    private @Nullable ShardManager       shardManager;

    public GatewayClient(MoonRisePlugin plugin, Storage storage) {
        super(storage);
        this.plugin = plugin;
        this.interactionManager = new InteractionManager(plugin);

        // Schedule the leaderboard manager to run scheduled tasks
        this.leaderboardManager = new LeaderboardManager(this);
        this.leaderboardManager.schedule();
    }

    public void connect(String token) {
        if (token.isBlank()) {
            this.plugin.getLogger().warn("Token is blank, skipping connection");
        } else {
            // Establish the connection with the remote gateway
            this.establishConnection(token);
        }
    }

    private void establishConnection(String token) {
        this.shardManager = DefaultShardManagerBuilder.createDefault(token)
            .enableIntents(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_VOICE_STATES
            )
            .enableCache(
                CacheFlag.ACTIVITY,
                CacheFlag.VOICE_STATE,
                CacheFlag.ONLINE_STATUS
            )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setEnableShutdownHook(false)
            .setActivity(Activity.playing(AbstractMoonRisePlugin.getPluginName()))
            .addEventListeners(this.interactionManager)
            .addEventListeners(
                new UserListener(this),
                new GuildListener(this),
                new MemberListener(this)
            )
            .addEventListeners(new ActivityListener(this))
            .addEventListeners(new ListenerAdapter() {

                @Override
                public void onReady(@NotNull ReadyEvent e) {
                    GatewayClient.this.onShardReady(e.getJDA());
                }

                @Override
                public void onShutdown(@NotNull ShutdownEvent e) {
                    GatewayClient.this.shutdownLatch.countDown();
                }
            })
            .build();
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    public InteractionManager getInteractionManager() {
        return this.interactionManager;
    }

    public LeaderboardManager getLeaderboard() {
        return this.leaderboardManager;
    }

    public Optional<ShardManager> getShardManager() {
        return Optional.ofNullable(this.shardManager);
    }

    /**
     * Awaits the shutdown of the {@link ShardManager} by blocking.
     *
     * <p>This method ensures that the gateway's shutdown process completes
     * before continuing further.</p>
     * <p>
     */
    private void awaitShutdown() {
        try {
            if (!this.shutdownLatch.await(30, TimeUnit.SECONDS)) { // blocking
                this.plugin.getLogger().severe("The gateway shutdown timed out!");
            }
        } catch (InterruptedException e) {
            this.plugin.getLogger().warn("Interrupted while waiting for gateway shutdown", e);
        }
    }

    @Override
    public void close() {
        this.leaderboardManager.close();

        if (this.shardManager != null) {
            this.shardManager.shutdown();
            this.awaitShutdown(); // blocking
        }
    }

    /**
     * Handles tasks  that should be performed when a shard becomes ready.
     */
    private void onShardReady(@NotNull JDA shard) {
        this.interactionManager.registerInteraction(shard);
    }

    @Override
    public Executor actionExecutor() {
        return this.plugin.getBootstrap().getScheduler().async();
    }
}