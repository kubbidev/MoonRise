package me.kubbidev.moonrise.common.dependencies;

import me.kubbidev.moonrise.common.database.DatabaseType;

import java.util.Set;

/**
 * Loads and manages runtime dependencies for the plugin.
 */
public interface DependencyManager extends AutoCloseable {

    /**
     * Loads dependencies.
     *
     * @param dependencies the dependencies to load
     */
    void loadDependencies(Set<Dependency> dependencies);

    /**
     * Loads database dependencies.
     *
     * @param databaseType the database type in use
     */
    void loadStorageDependencies(DatabaseType databaseType);

    /**
     * Obtains an isolated classloader containing the given dependencies.
     *
     * @param dependencies the dependencies
     * @return the classloader
     */
    ClassLoader obtainClassLoaderWith(Set<Dependency> dependencies);

    @Override
    void close();
}
