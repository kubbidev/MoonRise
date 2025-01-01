package me.kubbidev.moonrise.common.config.generic.adapter;

import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class EnvironmentVariableConfigAdapter extends StringBasedConfigurationAdapter {
    private static final String PREFIX = "MOONRISE_";

    private final MoonRisePlugin plugin;

    public EnvironmentVariableConfigAdapter(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> MOONRISE_SERVER
        // 'data.table_prefix' -> MOONRISE_DATA_TABLE_PREFIX
        String key = PREFIX + path.toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');

        String value = System.getenv(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger().info(String.format("Resolved configuration value from environment variable: %s = %s", key, printableValue));
        }
        return value;
    }

    @Override
    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void reload() {
        // no-op
    }
}