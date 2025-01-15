package me.kubbidev.moonrise.common.leaderboard.activity;

import com.google.common.util.concurrent.AtomicDouble;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents the voice state of a {@link me.kubbidev.moonrise.common.model.ApiMember}
 * <p>
 * This class is immutable and thread-safe for the fields related to activity,
 * using AtomicLong and AtomicDouble for concurrency purposes.
 */
public final class MemberState {
    /** Keeps track of the accumulated active time of a member in a voice state */
    private final AtomicLong activityTime = new AtomicLong(0);

    /** Represents the number of activity points accumulated by a member in a voice state */
    private final AtomicDouble activityPoints = new AtomicDouble(0);

    private long lastAccumulated = System.currentTimeMillis();
    private boolean frozen;

    public MemberState(boolean frozen) {
        this.frozen = frozen;
    }

    public @NotNull AtomicLong getActivityTime() {
        return this.activityTime;
    }

    public @NotNull AtomicDouble getActivityPoints() {
        return this.activityPoints;
    }

    public long getLastAccumulated() {
        return this.lastAccumulated;
    }

    public void setLastAccumulated(long lastAccumulated) {
        this.lastAccumulated = lastAccumulated;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
}
