package me.kubbidev.moonrise.common.database.implementation;

import me.kubbidev.moonrise.common.database.DatabaseMetadata;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

public interface DatabaseImplementation {
    MoonRisePlugin getPlugin();

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    DatabaseMetadata getMeta();
}
