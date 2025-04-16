package me.kubbidev.moonrise.common.plugin.scheduler;

/**
 * Represents a scheduled task
 */
@FunctionalInterface
public interface SchedulerTask {

    /**
     * Cancels the task.
     */
    void cancel();
}