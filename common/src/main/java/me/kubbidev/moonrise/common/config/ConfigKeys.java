package me.kubbidev.moonrise.common.config;

import me.kubbidev.moonrise.common.config.generic.KeyedConfiguration;
import me.kubbidev.moonrise.common.config.generic.key.ConfigKey;
import me.kubbidev.moonrise.common.config.generic.key.SimpleConfigKey;
import me.kubbidev.moonrise.common.database.DatabaseType;
import me.kubbidev.moonrise.common.config.generic.key.ConfigKeyFactory;
import me.kubbidev.moonrise.common.database.misc.DatabaseCredentials;

import java.util.List;
import java.util.Locale;

import static me.kubbidev.moonrise.common.config.generic.key.ConfigKeyFactory.*;

/**
 * All of the {@link ConfigKey}s used by MoonRise.
 *
 * <p>The {@link #getKeys()} method and associated behaviour allows this class
 * to function a bit like an enum, but with generics.</p>
 */
@SuppressWarnings("CodeBlock2Expr")
public final class ConfigKeys {

    /**
     * How many minutes to wait between syncs. A value <= 0 will disable syncing.
     */
    public static final ConfigKey<Integer> SYNC_TIME = notReloadable(ConfigKeyFactory.integerKey("sync-minutes", 10));

    /**
     * If MoonRise should automatically install translation bundles and periodically update them.
     */
    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = notReloadable(booleanKey("auto-install-translations", true));

    /**
     * If MoonRise should rate-limit command executions to users spamming.
     */
    public static final ConfigKey<Boolean> COMMANDS_RATE_LIMIT = booleanKey("commands-rate-limit", true);

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<DatabaseCredentials> DATABASE_VALUES = notReloadable(key(c -> {
        return new DatabaseCredentials(
                c.getString("data.address", null),
                c.getString("data.database", null),
                c.getString("data.username", null),
                c.getString("data.password", null)
        );
    }));

    /**
     * The prefix for any SQL tables
     */
    public static final ConfigKey<String> SQL_TABLE_PREFIX = notReloadable(key(c -> {
        return c.getString("data.table-prefix", c.getString("data.table_prefix", "moonrise_"));
    }));

    /**
     * The name of the database method being used
     */
    public static final ConfigKey<DatabaseType> DATABASE_METHOD = notReloadable(key(c -> {
        return DatabaseType.parse(c.getString("database-method", "h2"), DatabaseType.H2);
    }));

    /**
     * The URL of the bytebin instance used to upload data
     */
    public static final ConfigKey<String> BYTEBIN_URL = stringKey("bytebin-url", "https://bytebin.kubbidev.me/");

    /**
     * A list of the keys defined in this class.
     */
    private static final List<SimpleConfigKey<?>> KEYS = KeyedConfiguration.initialise(ConfigKeys.class);

    public static List<? extends ConfigKey<?>> getKeys() {
        return KEYS;
    }

    /**
     * Check if the value at the given path should be censored in console/log output
     *
     * @param path the path
     * @return true if the value should be censored
     */
    public static boolean shouldCensorValue(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.contains("password") || lower.contains("uri");
    }
}