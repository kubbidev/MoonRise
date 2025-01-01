package me.kubbidev.moonrise.common.database;

import com.google.common.collect.ImmutableList;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.database.implementation.DatabaseImplementation;
import me.kubbidev.moonrise.common.database.implementation.custom.CustomDatabaseProviders;
import me.kubbidev.moonrise.common.database.implementation.sql.SqlDatabase;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.file.H2ConnectionFactory;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.file.SqliteConnectionFactory;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.hikari.MariaDbConnectionFactory;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.hikari.MySqlConnectionFactory;
import me.kubbidev.moonrise.common.database.implementation.sql.connection.hikari.PostgresConnectionFactory;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public enum DatabaseType {

    // Remote databases
    MARIADB("mariadb") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlDatabase(
                    plugin,
                    new MariaDbConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                    plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    MYSQL("mysql") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlDatabase(
                    plugin,
                    new MySqlConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                    plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    POSTGRESQL("postgresql") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlDatabase(
                    plugin,
                    new PostgresConnectionFactory(plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                    plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },

    // Local databases
    SQLITE("sqlite") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlDatabase(
                    plugin,
                    new SqliteConnectionFactory(plugin.getBootstrap().getDataDirectory().resolve("moonrise-sqlite.db")),
                    plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },
    H2("h2") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return new SqlDatabase(
                    plugin,
                    new H2ConnectionFactory(plugin.getBootstrap().getDataDirectory().resolve("moonrise-h2-v2")),
                    plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
            );
        }
    },

    // Custom
    CUSTOM("custom") {
        @Override
        @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin) {
            return CustomDatabaseProviders.getProvider().provide(plugin);
        }
    };

    private final List<String> identifiers;

    DatabaseType(String... identifiers) {
        this.identifiers = ImmutableList.copyOf(identifiers);
    }

    public static DatabaseType parse(String name, DatabaseType def) {
        return Arrays.stream(values()).filter(t -> {
            for (String i : t.identifiers) {
                if (i.equalsIgnoreCase(name)) return true;
            }
            return false;
        }).findFirst().orElse(def);
    }

    abstract @NotNull DatabaseImplementation createNewImplementation(@NotNull MoonRisePlugin plugin);

    public static @NotNull DatabaseType getRequiredType(MoonRisePlugin plugin) {
        return plugin.getConfiguration().get(ConfigKeys.DATABASE_METHOD);
    }

    public static @NotNull Database getInstance(MoonRisePlugin plugin) {
        DatabaseType type = DatabaseType.getRequiredType(plugin);
        plugin.getLogger().info("Loading database provider... [" + type.name() + "]");

        Database database = new Database(plugin, type.createNewImplementation(plugin));
        database.init();
        return database;
    }
}
