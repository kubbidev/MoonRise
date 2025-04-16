package me.kubbidev.moonrise.common.leaderboard.tracker;

import com.github.benmanes.caffeine.cache.Cache;
import me.kubbidev.moonrise.common.leaderboard.activity.MemberState;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.CaffeineFactory;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * The GuildActivityTracker class is responsible for managing and tracking user activity in various guild channels.
 */
public final class GuildActivityTracker {

    private final MoonRisePlugin               plugin;
    private final Cache<Long, ActivityTracker> activityTrackers = CaffeineFactory.newBuilder()
        .expireAfterAccess(Duration.ofDays(10)).build();

    public GuildActivityTracker(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Determines if the activity tracker cache is empty.
     *
     * @return true if there are no activity trackers currently stored in the cache, false otherwise
     */
    public boolean isEmpty() {
        synchronized (this.activityTrackers) {
            return this.activityTrackers.estimatedSize() == 0;
        }
    }

    /**
     * Adds a channel to the activity tracker system and associates a user with that channel.
     * <p>
     * If no {@link ActivityTracker} exists for the given channelId, a new tracker is created.
     *
     * <p>The user is added to the tracker with the specified frozen state.</p>
     *
     * @param channelId The unique identifier of the channel to be added or updated in the activity tracker.
     * @param userId    The unique identifier of the user to be associated with the specified channel.
     * @param frozen    A boolean value indicating whether the user should initially be marked as frozen within the
     *                  activity tracker.
     */
    public void addChannel(long channelId, long userId, boolean frozen) {
        synchronized (this.activityTrackers) {
            var tracker = this.activityTrackers.get(channelId, __ -> new ActivityTracker(this.plugin));
            if (tracker != null) {
                tracker.addMember(userId, frozen);
            }
        }
    }

    /**
     * Removes a user from the {@link ActivityTracker} associated with the given channel.
     *
     * <p>If the user is successfully removed, the state of the removed user is returned.</p>
     * <p>
     * Additionally, if the activity tracker corresponding to the channel becomes empty after the removal, the tracker
     * is invalidated and removed from the cache.
     *
     * @param channelId The unique identifier of the channel from which the user is to be removed.
     * @param member    The unique identifier of the user to be removed from the activity tracker.
     * @return The {@link MemberState} of the removed user if the user was present in the tracker, or null if the user
     * or channel was not found.
     */
    public @Nullable MemberState removeChannel(long channelId, Member member) {
        synchronized (this.activityTrackers) {
            var tracker = this.activityTrackers.getIfPresent(channelId);
            if (tracker == null) {
                return null;
            }

            var state = tracker.removeMember(member);
            if (tracker.isEmpty()) {
                this.activityTrackers.invalidate(channelId);
            }
            return state;
        }
    }

    /**
     * Updates the frozen state of a specific user across all activity trackers.
     * <p>
     * This method iterates through all {@link ActivityTracker} instances stored in the `activityTrackers` cache and
     * applies the specified frozen state to the user with the given user id.
     * <p>
     * It ensures thread safety by synchronizing access to the `activityTrackers` map.
     *
     * @param userId The unique identifier of the user whose frozen state is to be modified.
     * @param frozen A boolean indicating the new frozen state for the user. Set to {@code true} to mark the user as
     *               frozen, or {@code false} to unfreeze.
     */
    public void freeze(long userId, boolean frozen) {
        synchronized (this.activityTrackers) {
            this.activityTrackers.asMap().values().forEach(tracker -> tracker.freeze(userId, frozen));
        }
    }
}
