package me.kubbidev.moonrise.common.api;

import net.moonrise.api.MoonRise;
import net.moonrise.api.MoonRiseProvider;
import net.moonrise.api.platform.Health;
import net.moonrise.api.platform.Platform;
import net.moonrise.api.platform.PluginMetadata;
import me.kubbidev.moonrise.common.api.implementation.ApiPlatform;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.bootstrap.BootstrappedWithLoader;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.plugin.logging.PluginLogger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Implements the MoonRise API using the plugin instance
 */
public class MoonRiseApiProvider implements MoonRise {
    private final MoonRisePlugin plugin;

    private final ApiPlatform platform;

    public MoonRiseApiProvider(MoonRisePlugin plugin) {
        this.plugin = plugin;
        this.platform = new ApiPlatform(plugin);
    }

    public void ensureApiWasLoadedByPlugin() {
        MoonRiseBootstrap bootstrap = this.plugin.getBootstrap();
        ClassLoader pluginClassLoader;
        if (bootstrap instanceof BootstrappedWithLoader) {
            pluginClassLoader = ((BootstrappedWithLoader) bootstrap).getLoader().getClass().getClassLoader();
        } else {
            pluginClassLoader = bootstrap.getClass().getClassLoader();
        }

        for (Class<?> apiClass : new Class[]{MoonRise.class, MoonRiseProvider.class}) {
            ClassLoader apiClassLoader = apiClass.getClassLoader();

            if (!apiClassLoader.equals(pluginClassLoader)) {
                String guilty = "unknown";
                try {
                    guilty = bootstrap.identifyClassLoader(apiClassLoader);
                } catch (Exception e) {
                    // ignore
                }

                PluginLogger logger = this.plugin.getLogger();
                logger.warn("It seems that the MoonRise API has been (class)loaded by a plugin other than MoonRise!");
                logger.warn("The API was loaded by " + apiClassLoader + " (" + guilty + ") and the " +
                        "MoonRise plugin was loaded by " + pluginClassLoader.toString() + ".");
                logger.warn("This indicates that the other plugin has incorrectly \"shaded\" the " +
                        "MoonRise API into its jar file. This can cause errors at runtime and should be fixed.");
                return;
            }
        }
    }

    @Override
    public @NotNull Platform getPlatform() {
        return this.platform;
    }

    @Override
    public @NotNull PluginMetadata getPluginMetadata() {
        return this.platform;
    }

    @Override
    public @NotNull CompletableFuture<Void> runUpdateTask() {
        return this.plugin.getSyncTaskBuffer().request();
    }

    @Override
    public @NotNull Health runHealthCheck() {
        return this.plugin.runHealthCheck();
    }
}
