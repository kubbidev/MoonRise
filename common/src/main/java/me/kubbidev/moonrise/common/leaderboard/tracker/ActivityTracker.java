package me.kubbidev.moonrise.common.leaderboard.tracker;

import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.leaderboard.activity.MemberState;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that tracks and calculates the voice activity of members in a synchronized and thread-safe manner.
 */
public class ActivityTracker {

    private final MoonRisePlugin         plugin;
    /**
     * A thread-safe map that holds the voice activity states of members in the form of {@link MemberState}
     */
    private final Map<Long, MemberState> states = new ConcurrentHashMap<>();

    public ActivityTracker(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if the collection of voice activity states is empty.
     *
     * @return true if the collection of states is empty, false otherwise
     */
    public boolean isEmpty() {
        synchronized (this.states) {
            return this.states.isEmpty();
        }
    }

    /**
     * Adds a new member to the {@link ActivityTracker} with a specified frozen state.
     * <p>
     * Invokes the accumulate method to ensure current activity metrics are updated prior to adding a new member.
     *
     * @param userId The unique identifier of the member to be added.
     * @param frozen A boolean indicating whether the member should initially be marked as frozen.
     */
    public void addMember(long userId, boolean frozen) {
        synchronized (this.states) {
            this.accumulate();
            this.states.put(userId, new MemberState(frozen));
        }
    }

    /**
     * Removes a member from the {@link ActivityTracker} associated with the given user id.
     * <p>
     * Invokes the accumulate method to ensure current activity metrics are updated before the removal.
     *
     * @param member The member to be removed.
     * @return The state of the removed {@link MemberState} if a member with the specified id existed, or null if no
     * such member was found.
     */
    public synchronized @Nullable MemberState removeMember(Member member) {
        synchronized (this.states) {
            this.accumulate();
            return this.states.remove(member.getIdLong());
        }
    }

    /**
     * Updates the frozen state of a specific {@link MemberState} in the voice activity tracker.
     * <p>
     * Invokes the accumulate method to ensure any ongoing activity metrics are updated before altering the member's
     * frozen state.
     * <p>
     * If a member with the specified id exists, its frozen state is updated to the provided value.
     *
     * @param userId The unique identifier of the member whose frozen state is to be modified.
     * @param frozen A boolean value indicating the new frozen state for the member. Set to {@code true} to mark the
     *               member as frozen, or {@code false} to unfreeze.
     */
    public synchronized void freeze(long userId, boolean frozen) {
        synchronized (this.states) {
            this.accumulate();
            var state = this.states.get(userId);
            if (state != null) {
                state.setFrozen(frozen);
            }
        }
    }

    /**
     * Accumulates activity metrics for all {@link MemberState} currently tracked in the voice states.
     * <p>
     * <ul>
     * <li>Iterates through each {@link MemberState} in the `states` map and updates its activity metrics,
     * including activity time and activity points, based on its current state.</li>
     *
     * <li>Ignores members that are marked as frozen using the {@code isFrozen} flag.</li>
     * <li>Caps the number of active members contributing to the activity calculations to {@code MAX_VOICES},
     * if {@code MAX_VOICES} is greater than 0.</li>
     * </ul>
     *
     * <b>Key considerations:</b>
     * <ul>
     * <li>Members are only eligible for activity accumulation if they are not frozen, as determined by
     * {@link MemberState#isFrozen()}.</li>
     *
     * <li>Activity time and points are computed based on the duration since the last accumulation and the
     * current count of active speaking members.</li>
     *
     * <li>Ensures concurrent updates to {@link MemberState#getActivityTime()} and
     * {@link MemberState#getActivityPoints()} remain thread-safe using atomic operations.</li>
     *
     * <li>Updates the `lastAccumulated` timestamp for all members to the current time, even when no activity
     * data is modified.</li>
     * </ul>
     */
    private void accumulate() {
        long currentMillis = System.currentTimeMillis();
        long speakingMembers = this.states.values().stream().filter(m -> !m.isFrozen()).count();

        int maxVoiceMembers = this.plugin.getConfiguration().get(ConfigKeys.ACTIVITY_MAX_VOICES);
        if (maxVoiceMembers > 0) {
            speakingMembers = Math.min(speakingMembers, maxVoiceMembers);
        }

        for (MemberState value : this.states.values()) {
            if (!value.isFrozen() && speakingMembers > 1) {
                long duration = currentMillis - value.getLastAccumulated();

                value.getActivityTime().addAndGet(duration);
                value.getActivityPoints().addAndGet((duration / 60000.0F * speakingMembers * 0.4F));
            }

            value.setLastAccumulated(currentMillis);
        }
    }
}
