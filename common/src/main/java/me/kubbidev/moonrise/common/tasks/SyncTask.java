package me.kubbidev.moonrise.common.tasks;

import me.kubbidev.moonrise.common.cache.BufferedRequest;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.util.concurrent.TimeUnit;

/**
 * System wide sync task for MoonRise.
 *
 * <p>Ensures that all local data is consistent with the storage.</p>
 */
public class SyncTask implements Runnable {
    private final MoonRisePlugin plugin;

    public SyncTask(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Runs the update task
     *
     * <p>Called <b>async</b>.</p>
     */
    @Override
    public void run() {
        if (this.plugin.getEventDispatcher().dispatchPreSync(false)) {
            return;
        }

        this.plugin.performPlatformDataSync();
        this.plugin.getEventDispatcher().dispatchPostSync();
    }

    public static class Buffer extends BufferedRequest<Void> {
        private final MoonRisePlugin plugin;

        public Buffer(MoonRisePlugin plugin) {
            super(500L, TimeUnit.MILLISECONDS, plugin.getBootstrap().getScheduler());
            this.plugin = plugin;
        }

        @Override
        protected Void perform() {
            new SyncTask(this.plugin).run();
            return null;
        }
    }
}