package me.kubbidev.moonrise.api;

import me.kubbidev.moonrise.api.event.EventBus;
import me.kubbidev.moonrise.api.platform.PluginMetadata;
import me.kubbidev.moonrise.api.platform.Health;
import me.kubbidev.moonrise.api.platform.Platform;
import org.jetbrains.annotations.NotNull;

/**
 * The MoonRise API.
 *
 * <p>The API allows other plugins on the server to read and modify MoonRise
 * data, change behaviour of the plugin, listen to certain events, and integrate MoonRise into other plugins and
 * systems.</p>
 *
 * <p>This interface represents the base of the API package. All functions are
 * accessed via this interface.</p>
 *
 * <p>To start using the API, you need to obtain an instance of this interface.
 * These are registered by the MoonRise plugin to the platforms Services Manager. This is the preferred method for
 * obtaining an instance.</p>
 *
 * <p>For ease of use, and for platforms without a Service Manager, an instance
 * can also be obtained from the static singleton accessor in {@link MoonRiseProvider}.</p>
 */
public interface MoonRise {

    /**
     * Gets the {@link Platform}, which represents the server platform the plugin is running on.
     *
     * @return the platform
     */
    @NotNull Platform getPlatform();

    /**
     * Gets the {@link PluginMetadata}, responsible for providing metadata about the MoonRise plugin currently running.
     *
     * @return the plugin metadata
     */
    @NotNull PluginMetadata getPluginMetadata();

    /**
     * Gets the {@link EventBus}, used for subscribing to internal MoonRise events.
     *
     * @return the event bus
     */
    @NotNull EventBus getEventBus();

    /**
     * Executes a health check.
     *
     * <p>This task checks if the MoonRise implementation is running and
     * whether it has a connection to the database (if applicable).</p>
     *
     * @return the health status
     */
    @NotNull Health runHealthCheck();
}
