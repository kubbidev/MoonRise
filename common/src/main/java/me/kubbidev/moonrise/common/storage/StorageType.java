package me.kubbidev.moonrise.common.storage;

import com.google.common.collect.ImmutableList;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.storage.implementation.custom.CustomStorageProviders;
import me.kubbidev.moonrise.common.storage.implementation.sql.SqlStorage;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.file.H2ConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.file.SqliteConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.hikari.MariaDbConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.kubbidev.moonrise.common.storage.implementation.sql.connection.hikari.PostgresConnectionFactory;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public enum StorageType {

    // Remote databases
    MARIADB("mariadb") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlStorage(
                plugin,
                new MariaDbConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    MYSQL("mysql") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlStorage(
                plugin,
                new MySqlConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    POSTGRESQL("postgresql") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlStorage(
                plugin,
                new PostgresConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },

    // Local databases
    SQLITE("sqlite") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlStorage(
                plugin,
                new SqliteConnectionFactory(plugin.getBootstrap().getDataDirectory().resolve("moonrise-sqlite.db")),
                plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    H2("h2") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlStorage(
                plugin,
                new H2ConnectionFactory(plugin.getBootstrap().getDataDirectory().resolve("moonrise-h2-v2")),
                plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },

    // Custom
    CUSTOM("custom") {
        @Override
        @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return CustomStorageProviders.getProvider().provide(plugin);
        }
    };

    private final List<String> identifiers;

    StorageType(String... identifiers) {
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static StorageType parse(String name, StorageType def) {
        return Arrays.stream(values()).filter(t -> {
            for (String i : t.identifiers) {
                if (i.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }).findFirst().orElse(def);
    }

    abstract @NotNull StorageImplementation createNewImplementation(@NotNull MoonRisePlugin plugin);

    public static @NotNull StorageType getRequiredType(MoonRisePlugin plugin) {
        return plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
    }

    public static @NotNull Storage getInstance(MoonRisePlugin plugin) {
        StorageType type = StorageType.getRequiredType(plugin);
        plugin.getLogger().info("Loading storage provider... [" + type.name() + "]");

        Storage storage = new Storage(plugin, type.createNewImplementation(plugin));
        storage.init();
        return storage;
    }
}
