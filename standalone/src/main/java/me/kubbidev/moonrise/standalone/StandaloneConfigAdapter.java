package me.kubbidev.moonrise.standalone;

import me.kubbidev.moonrise.common.config.generic.adapter.ConfigurateConfigAdapter;
import me.kubbidev.moonrise.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.nio.file.Path;

public class StandaloneConfigAdapter extends ConfigurateConfigAdapter implements ConfigurationAdapter {
    public StandaloneConfigAdapter(MoonRisePlugin plugin, Path path) {
        super(plugin, path);
    }

    @Override
    protected ConfigurationLoader<? extends ConfigurationNode> createLoader(Path path) {
        return YAMLConfigurationLoader.builder().setPath(path).build();
    }
}