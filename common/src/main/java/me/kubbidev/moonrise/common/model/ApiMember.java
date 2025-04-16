package me.kubbidev.moonrise.common.model;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.storage.misc.DataConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * A Discord guild member.
 *
 * @see <a href="https://discord.com/developers/docs/resources/guild#guild-member-object">Guild Member Object</a>
 */
public class ApiMember extends ApiUser {

    private           long   guildId;
    private @Nullable String nickname    = null;
    private @Nullable String guildAvatar = null;
    private @Nullable String biography   = null;
    private           long   experience;
    private           long   voiceActivity;
    /**
     * Represents the previous placement of a member in a leaderboard or ranking system
     */
    private           int    placement   = -1;

    public ApiMember(long id, MoonRisePlugin plugin) {
        super(id, plugin);
    }

    public long getGuildId() {
        return this.guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    /**
     * Gets the nickname associated with the member.
     *
     * <p>Returns {@code empty} if no nickname is known for the member.</p>
     *
     * @return the member's nickname, or {@code empty} if unavailable.
     */
    public @NotNull Optional<String> getNickname() {
        return Optional.ofNullable(this.nickname);
    }

    public void setNickname(@Nullable String nickname) {
        this.nickname = DataConstraints.sanitize(nickname);
    }

    /**
     * Gets the name that is displayed in client.
     *
     * @return The name displayed in client.
     */
    @Override
    public @NotNull String getDisplayName() {
        return getNickname().orElseGet(super::getDisplayName);
    }

    /**
     * Gets the URL of the {@link ApiMember}'s guild avatar.
     *
     * <p>Returns {@code null} if the member does not have a guild avatar set.</p>
     *
     * @return the member's guild avatar URL, or {@code null} if not set.
     */
    public @Nullable String getGuildAvatar() {
        return this.guildAvatar;
    }

    public void setGuildAvatar(@Nullable String guildAvatar) {
        this.guildAvatar = DataConstraints.sanitize(guildAvatar);
    }

    /**
     * Gets if the member's guild avatar is animated.
     *
     * @return {@code true} if the member's guild avatar is animated, {@code false} otherwise.
     */
    public boolean hasAnimatedGuildAvatar() {
        String avatar = getGuildAvatar();
        return avatar != null && avatar.startsWith("a_");
    }

    /**
     * Gets the member's effective avatar URL.
     *
     * <p>If the member does not have a guild avatar, this defaults to the user's global avatar.</p>
     *
     * @return The member's effective avatar URL.
     */
    public @NotNull String getDisplayedAvatar() {
        String avatar = getGuildAvatar();
        return avatar != null ? avatar : getAvatar();
    }

    /**
     * Gets the biography associated with the member.
     *
     * <p>Returns {@code empty} if no biography is known for the member.</p>
     *
     * @return the member's biography, or {@code empty} if unavailable.
     */
    public @NotNull Optional<String> getBiography() {
        return Optional.ofNullable(this.biography);
    }

    /**
     * Sets the biography associated with the member.
     *
     * @param biography the member's biography
     */
    public void setBiography(@Nullable String biography) {
        this.biography = DataConstraints.sanitize(biography);
    }

    public long getExperience() {
        return this.experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public long getVoiceActivity() {
        return this.voiceActivity;
    }

    public void setVoiceActivity(long voiceActivity) {
        this.voiceActivity = voiceActivity;
    }

    public int getPlacement() {
        return this.placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ApiMember member)) {
            return false;
        }
        return super.equals(o) && this.guildId == member.guildId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.guildId);
    }
}
