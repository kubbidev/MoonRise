package me.kubbidev.moonrise.common.config;

import me.kubbidev.moonrise.common.config.generic.KeyedConfiguration;
import me.kubbidev.moonrise.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

public class MoonRiseConfiguration extends KeyedConfiguration {

    private final MoonRisePlugin plugin;

    public MoonRiseConfiguration(MoonRisePlugin plugin, ConfigurationAdapter adapter) {
        super(adapter, ConfigKeys.getKeys());
        this.plugin = plugin;
    }

    @Override
    public void reload() {
        super.reload();
        this.plugin.getEventDispatcher().dispatchConfigReload();
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }
}
