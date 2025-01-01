package net.moonrise.api.event.sync;

import net.moonrise.api.event.MoonRiseEvent;
import net.moonrise.api.event.type.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Called just before a full synchronisation task runs.
 */
public class PreSyncEvent implements MoonRiseEvent, Cancellable {
    private final AtomicBoolean cancellationState = new AtomicBoolean();

    @Override
    public @NotNull AtomicBoolean cancellationState() {
        return this.cancellationState;
    }
}
