package me.kubbidev.moonrise.common.database;

import me.kubbidev.moonrise.common.database.implementation.DatabaseImplementation;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.util.AsyncInterface;

import java.util.concurrent.CompletableFuture;

/**
 * Provides a {@link CompletableFuture} based API for interacting with a {@link DatabaseImplementation}.
 */
public class Database extends AsyncInterface {
    private final MoonRisePlugin plugin;
    private final DatabaseImplementation implementation;

    public Database(MoonRisePlugin plugin, DatabaseImplementation implementation) {
        super(plugin);
        this.plugin = plugin;
        this.implementation = implementation;
    }

    public DatabaseImplementation getImplementation() {
        return this.implementation;
    }

    public String getName() {
        return this.implementation.getImplementationName();
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to init database implementation", e);
        }
    }

    public void shutdown() {
        try {
            this.implementation.shutdown();
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to shutdown database implementation", e);
        }
    }

    public DatabaseMetadata getMeta() {
        return this.implementation.getMeta();
    }
}
