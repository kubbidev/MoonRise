package me.kubbidev.moonrise.common.api.implementation;

import me.kubbidev.api.platform.Platform;
import me.kubbidev.api.platform.PluginMetadata;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class ApiPlatform implements Platform, PluginMetadata {
    private final MoonRisePlugin plugin;

    public ApiPlatform(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Type getType() {
        return this.plugin.getBootstrap().getType();
    }

    @Override
    public @NotNull Instant getStartTime() {
        return this.plugin.getBootstrap().getStartupTime();
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getBootstrap().getVersion();
    }

    @Override
    public @NotNull String getApiVersion() {
        String[] version = this.plugin.getBootstrap().getVersion().split("\\.");
        return version[0] + '.' + version[1];
    }
}
