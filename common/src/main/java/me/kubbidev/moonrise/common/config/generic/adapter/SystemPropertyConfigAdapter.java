package me.kubbidev.moonrise.common.config.generic.adapter;

import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import org.jetbrains.annotations.Nullable;

public class SystemPropertyConfigAdapter extends StringBasedConfigurationAdapter {

    private static final String         PREFIX = "moonrise.";
    private final        MoonRisePlugin plugin;

    public SystemPropertyConfigAdapter(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected @Nullable String resolveValue(String path) {
        // e.g.
        // 'server'            -> moonrise.server
        // 'data.table_prefix' -> moonrise.data.table-prefix
        String key = PREFIX + path;

        String value = System.getProperty(key);
        if (value != null) {
            String printableValue = ConfigKeys.shouldCensorValue(path) ? "*****" : value;
            this.plugin.getLogger()
                .info(String.format("Resolved configuration value from system property: %s = %s", key, printableValue));
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