package me.kubbidev.moonrise.common.database.implementation.custom;

import me.kubbidev.moonrise.common.database.implementation.DatabaseImplementation;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

/**
 * A database provider
 */
@FunctionalInterface
public interface CustomDatabaseProvider {

    DatabaseImplementation provide(MoonRisePlugin plugin);
}