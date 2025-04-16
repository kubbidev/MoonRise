package me.kubbidev.moonrise.common.config;

import me.kubbidev.moonrise.common.config.generic.KeyedConfiguration;
import me.kubbidev.moonrise.common.config.generic.key.ConfigKey;
import me.kubbidev.moonrise.common.config.generic.key.SimpleConfigKey;
import me.kubbidev.moonrise.common.storage.StorageType;
import me.kubbidev.moonrise.common.config.generic.key.ConfigKeyFactory;
import me.kubbidev.moonrise.common.storage.misc.StorageCredentials;

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
     * The Discord application authentication token used to connect.
     */
    public static final ConfigKey<String> AUTHENTICATION_TOKEN = notReloadable(
        ConfigKeyFactory.stringKey("authentication-token", ""));

    /**
     * If MoonRise should automatically install translation bundles and periodically update them.
     */
    public static final ConfigKey<Boolean> AUTO_INSTALL_TRANSLATIONS = notReloadable(
        booleanKey("auto-install-translations", true));

    /**
     * If MoonRise should rate-limit command executions to users spamming.
     */
    public static final ConfigKey<Boolean> COMMANDS_RATE_LIMIT = booleanKey("commands-rate-limit", true);

    /**
     * Represents the maximum number of activity voices that can be used concurrently.
     */
    public static final ConfigKey<Integer> ACTIVITY_MAX_VOICES = integerKey("activity-max-voices", 5);

    /**
     * A configuration key representing the multiplier applied to activity-based experience calculations.
     */
    public static final ConfigKey<Integer> ACTIVITY_EXPERIENCE_MULTIPLIER = integerKey("activity-experience-multiplier",
        1);

    /**
     * The database settings, username, password, etc for use by any database
     */
    public static final ConfigKey<StorageCredentials> DATABASE_VALUES = notReloadable(key(c -> {
        return new StorageCredentials(
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
     * The name of the storage method being used
     */
    public static final ConfigKey<StorageType> STORAGE_METHOD = notReloadable(key(c -> {
        return StorageType.parse(c.getString("storage-method", "h2"), StorageType.H2);
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
        return lower.contains("password") || lower.contains("uri") || lower.contains("token");
    }
}