package me.kubbidev.moonrise.common.model;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.storage.misc.DataConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

/**
 * A Discord user.
 *
 * @see <a href="https://discord.com/developers/docs/resources/user">Users Resource</a>
 */
public class ApiUser extends Snowflake {

    /**
     * The path for default user avatar image URLs.
     */
    public static final String         DEFAULT_IMAGE_PATH = "https://cdn.discordapp.com/embed/avatars/%s.png";
    protected final     MoonRisePlugin plugin;
    private @Nullable   String         username           = null;
    private @Nullable   String         globalName         = null;
    private @Nullable   String         avatar             = null;
    private @NotNull    Instant        lastSeen           = Instant.now();

    public ApiUser(long id, MoonRisePlugin plugin) {
        super(id);
        this.plugin = plugin;
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Gets the unique username associated with the user.
     *
     * <p>Returns null if no username is known for the user.</p>
     *
     * @return the user's username.
     */
    public @Nullable String getUsername() {
        return this.username;
    }

    public void setUsername(@Nullable String username) {
        this.username = DataConstraints.sanitize(username, true);
    }

    public boolean isUsernameMatching(@Nullable String name) {
        return this.username != null && this.username.equalsIgnoreCase(name);
    }

    /**
     * Gets a display name for this user.
     *
     * @return the display name
     */
    public @NotNull String getPlainDisplayName() {
        return this.username == null ? this.asString() : this.username;
    }

    /**
     * Gets the name that is displayed in client.
     *
     * @return The name displayed in client.
     */
    public @NotNull String getDisplayName() {
        return getGlobalName().orElseGet(this::getPlainDisplayName);
    }

    /**
     * Gets the user's global username, not enforced to be unique.
     *
     * <p>May be {@code empty} if the user has not set a global username.</p>
     *
     * @return The user's global name, or {@code empty} if unavailable.
     */
    public @NotNull Optional<String> getGlobalName() {
        return Optional.ofNullable(this.globalName);
    }

    public void setGlobalName(@Nullable String globalName) {
        this.globalName = DataConstraints.sanitize(globalName);
    }

    /**
     * Gets the URL of the user's avatar.
     *
     * @return the user's avatar URL.
     */
    public @NotNull String getAvatar() {
        return this.avatar == null ? this.getDefaultAvatar() : this.avatar;
    }

    public void setAvatar(@Nullable String avatar) {
        this.avatar = DataConstraints.sanitize(avatar);
    }

    /**
     * Gets if the user's avatar is animated.
     *
     * @return {@code true} if the user's avatar is animated, {@code false} otherwise.
     */
    public boolean hasAnimatedAvatar() {
        return this.getAvatar().startsWith("a_");
    }

    /**
     * Gets the default avatar URL for this user.
     *
     * @return the user's default avatar URL.
     */
    public @NotNull String getDefaultAvatar() {
        return DEFAULT_IMAGE_PATH.formatted((this.id >> 22) % 6);
    }

    /**
     * Gets the timestamp of the user's last recorded activity.
     *
     * @return an {@link Instant} representing the user's last seen time.
     */
    public @NotNull Instant getLastSeen() {
        return this.lastSeen;
    }

    /**
     * Sets the timestamp of the user's last recorded activity.
     *
     * @param lastSeen the last seen {@link Instant}
     */
    public void setLastSeen(@NotNull Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}