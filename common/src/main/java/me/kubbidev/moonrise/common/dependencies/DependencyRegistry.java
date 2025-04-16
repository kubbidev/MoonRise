package me.kubbidev.moonrise.common.dependencies;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import me.kubbidev.moonrise.api.platform.Platform;
import me.kubbidev.moonrise.common.storage.StorageType;
import me.kubbidev.moonrise.common.dependencies.relocation.Relocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies MoonRise specific behaviour for {@link Dependency}s.
 */
public class DependencyRegistry {

    private static final SetMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES = ImmutableSetMultimap.<StorageType, Dependency>builder()
            .putAll(StorageType.MARIADB,        Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI, Dependency.MARIADB_DRIVER)
            .putAll(StorageType.MYSQL,          Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI, Dependency.MYSQL_DRIVER)
            .putAll(StorageType.POSTGRESQL,     Dependency.SLF4J_API, Dependency.SLF4J_SIMPLE, Dependency.HIKARI, Dependency.POSTGRESQL_DRIVER)
            .putAll(StorageType.SQLITE,         Dependency.SQLITE_DRIVER)
            .putAll(StorageType.H2,             Dependency.H2_DRIVER)
            .build();

    private static final Set<Platform.Type> SNAKEYAML_PROVIDED_BY_PLATFORM = ImmutableSet.of(
        // empty
    );

    private final Platform.Type platformType;

    public DependencyRegistry(Platform.Type platformType) {
        this.platformType = platformType;
    }

    public Set<Dependency> resolveStorageDependencies(StorageType storageType) {
        Set<Dependency> dependencies = new LinkedHashSet<>(STORAGE_DEPENDENCIES.get(storageType));

        // don't load slf4j if it's already present
        if ((dependencies.contains(Dependency.SLF4J_API) || dependencies.contains(Dependency.SLF4J_SIMPLE))
            && slf4jPresent()) {
            dependencies.remove(Dependency.SLF4J_API);
            dependencies.remove(Dependency.SLF4J_SIMPLE);
        }

        // don't load snakeyaml if it's provided by the platform
        if (dependencies.contains(Dependency.SNAKEYAML) && SNAKEYAML_PROVIDED_BY_PLATFORM.contains(this.platformType)) {
            dependencies.remove(Dependency.SNAKEYAML);
        }

        return dependencies;
    }

    public void applyRelocationSettings(Dependency dependency, List<Relocation> relocations) {
        // relocate yaml within configurate if its being provided by MoonRise
        if (dependency == Dependency.CONFIGURATE_YAML && !SNAKEYAML_PROVIDED_BY_PLATFORM.contains(this.platformType)) {
            relocations.add(Relocation.of("yaml", "org{}yaml{}snakeyaml"));
        }
    }

    public boolean shouldAutoLoad(Dependency dependency) {
        return switch (dependency) {
            // all used within 'isolated' classloaders, and are therefore not
            // relocated.
            case ASM, ASM_COMMONS, JAR_RELOCATOR, H2_DRIVER, H2_DRIVER_LEGACY, SQLITE_DRIVER -> false;
            default -> true;
        };
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean slf4jPresent() {
        return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
    }
}