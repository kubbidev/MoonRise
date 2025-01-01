package me.kubbidev.api.event.type;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents an event that can be cancelled
 */
public interface Cancellable {

    /**
     * Gets an {@link AtomicBoolean} holding the cancellation state of the event
     *
     * @return the cancellation
     */
    @NotNull AtomicBoolean cancellationState();

    /**
     * Returns true if the event is currently cancelled.
     *
     * @return if the event is cancelled
     */
    default boolean isCancelled() {
        return this.cancellationState().get();
    }

    /**
     * Returns true if the event is not currently cancelled.
     *
     * @return if the event is not cancelled
     */
    default boolean isNotCancelled() {
        return !this.isCancelled();
    }

    /**
     * Sets the cancellation state of the event.
     *
     * @param cancelled the new state
     * @return the previous state
     */
    default boolean setCancelled(boolean cancelled) {
        return this.cancellationState().getAndSet(cancelled);
    }
}