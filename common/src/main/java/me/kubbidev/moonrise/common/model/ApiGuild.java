package me.kubbidev.moonrise.common.model;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.storage.misc.DataConstraints;
import org.jetbrains.annotations.Nullable;

/**
 * A Discord guild.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild">Guild Resource</a>
 */
public class ApiGuild extends Snowflake {
    private final MoonRisePlugin plugin;

    /** The last known name of a guild */
    private @Nullable String name = null;

    /** The last known icon url of a guild */
    private @Nullable String icon = null;

    private boolean leaderboardEnabled = false;
    private long leaderboardChannelId = 0;

    public ApiGuild(long id, MoonRisePlugin plugin) {
        super(id);
        this.plugin = plugin;
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Gets the name associated with the guild.
     *
     * <p>Returns {@code null} if no name is known for the guild.</p>
     *
     * @return the guild's name, or {@code null} if unavailable.
     */
    public @Nullable String getName() {
        return this.name;
    }

    public void setName(@Nullable String name) {
        this.name = DataConstraints.sanitize(name);
    }

    /**
     * Gets the URL of the guild's icon.
     *
     * <p>Returns {@code null} if the guild does not have an icon set.</p>
     *
     * @return the guild's icon URL, or {@code null} if not set.
     */
    public @Nullable String getIcon() {
        return this.icon;
    }

    public void setIcon(@Nullable String icon) {
        this.icon = DataConstraints.sanitize(icon);
    }

    public boolean isLeaderboardEnabled() {
        return this.leaderboardEnabled;
    }

    public void setLeaderboardEnabled(boolean leaderboardEnabled) {
        this.leaderboardEnabled = leaderboardEnabled;
    }

    public long getLeaderboardChannelId() {
        return this.leaderboardChannelId;
    }

    public void setLeaderboardChannelId(long leaderboardChannelId) {
        this.leaderboardChannelId = leaderboardChannelId;
    }
}
