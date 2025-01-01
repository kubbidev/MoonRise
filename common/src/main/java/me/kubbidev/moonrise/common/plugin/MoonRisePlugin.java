package me.kubbidev.moonrise.common.plugin;

import me.kubbidev.api.platform.Health;
import me.kubbidev.moonrise.common.api.MoonRiseApiProvider;
import me.kubbidev.moonrise.common.command.CommandManager;
import me.kubbidev.moonrise.common.command.abstraction.Command;
import me.kubbidev.moonrise.common.database.Database;
import me.kubbidev.moonrise.common.event.EventDispatcher;
import me.kubbidev.moonrise.common.extension.SimpleExtensionManager;
import me.kubbidev.moonrise.common.config.MoonRiseConfiguration;
import me.kubbidev.moonrise.common.dependencies.DependencyManager;
import me.kubbidev.moonrise.common.http.BytebinClient;
import me.kubbidev.moonrise.common.locale.TranslationRepository;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.plugin.logging.PluginLogger;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.locale.TranslationManager;
import me.kubbidev.moonrise.common.tasks.SyncTask;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Main internal interface for MoonRise plugins, providing the base for
 * abstraction throughout the project.
 * <p>
 * All plugin platforms implement this interface.
 */
public interface MoonRisePlugin {

    /**
     * Gets the bootstrap plugin instance
     *
     * @return the bootstrap plugin
     */
    MoonRiseBootstrap getBootstrap();

    /**
     * Gets the plugin's configuration
     *
     * @return the plugin config
     */
    MoonRiseConfiguration getConfiguration();

    /**
     * Gets the primary database instance. This is likely to be wrapped with extra layers for caching, etc.
     *
     * @return the database handler instance
     */
    Database getDatabase();

    /**
     * Gets a wrapped logger instance for the platform.
     *
     * @return the plugin's logger
     */
    PluginLogger getLogger();

    /**
     * Gets the event dispatcher
     *
     * @return the event dispatcher
     */
    EventDispatcher getEventDispatcher();

    /**
     * Returns the class implementing the MoonRiseAPI on this platform.
     *
     * @return the api
     */
    MoonRiseApiProvider getApiProvider();

    /**
     * Gets the extension manager.
     *
     * @return the extension manager
     */
    SimpleExtensionManager getExtensionManager();

    /**
     * Gets the command manager
     *
     * @return the command manager
     */
    CommandManager getCommandManager();

    /**
     * Gets the instance providing locale translations for the plugin
     *
     * @return the translation manager
     */
    TranslationManager getTranslationManager();

    /**
     * Gets the translation repository
     *
     * @return the translation repository
     */
    TranslationRepository getTranslationRepository();

    /**
     * Gets the dependency manager for the plugin
     *
     * @return the dependency manager
     */
    DependencyManager getDependencyManager();

    /**
     * Gets the bytebin instance in use by platform.
     *
     * @return the bytebin instance
     */
    BytebinClient getBytebin();

    /**
     * Runs a health check for the plugin.
     *
     * @return the result of the healthcheck
     */
    Health runHealthCheck();

    /**
     * Gets a list of online Senders on the platform
     *
     * @return a {@link List} of senders online on the platform
     */
    Stream<Sender> getOnlineSenders();

    /**
     * Gets the console.
     *
     * @return the console sender of the instance
     */
    Sender getConsoleSender();

    default List<Command<?>> getExtraCommands() {
        return Collections.emptyList();
    }

    /**
     * Gets the sync task buffer of the platform, used for scheduling and running sync tasks.
     *
     * @return the sync task buffer instance
     */
    SyncTask.Buffer getSyncTaskBuffer();

    /**
     * Called at the end of the sync task.
     */
    default void performPlatformDataSync() {

    }
}