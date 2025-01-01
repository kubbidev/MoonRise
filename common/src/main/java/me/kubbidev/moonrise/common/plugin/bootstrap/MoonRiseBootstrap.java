package me.kubbidev.moonrise.common.plugin.bootstrap;

import net.moonrise.api.platform.Platform;
import me.kubbidev.moonrise.common.plugin.classpath.ClassPathAppender;
import me.kubbidev.moonrise.common.plugin.logging.PluginLogger;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

/**
 * Bootstrap plugin interface
 *
 * <p>Instances of this interface are responsible for loading the
 * "MoonRise plugin" on their respective platforms.</p>
 */
public interface MoonRiseBootstrap {

    /**
     * Gets the plugin logger
     *
     * @return the logger
     */
    PluginLogger getPluginLogger();

    /**
     * Gets an adapter for the platforms scheduler
     *
     * @return the scheduler
     */
    SchedulerAdapter getScheduler();

    /**
     * Gets a {@link ClassPathAppender} for this bootstrap plugin
     *
     * @return a class path appender
     */
    ClassPathAppender getClassPathAppender();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has loaded.
     *
     * @return a loading latch
     */
    CountDownLatch getLoadLatch();

    /**
     * Returns a countdown latch which {@link CountDownLatch#countDown() counts down}
     * after the plugin has enabled.
     *
     * @return an enable latch
     */
    CountDownLatch getEnableLatch();

    /**
     * Gets a string of the plugin's version
     *
     * @return the version of the plugin
     */
    String getVersion();

    /**
     * Gets the time when the plugin first started in millis.
     *
     * @return the enable time
     */
    Instant getStartupTime();

    /**
     * Gets the duration between the plugin start from now.
     *
     * @return the duration from startup
     */
    default Duration getStartupDuration() {
        return Duration.between(getStartupTime(), Instant.now());
    }

    /**
     * Gets the platform type this instance of MoonRise is running on.
     *
     * @return the platform type
     */
    Platform.Type getType();

    /**
     * Gets the name or "brand" of the running platform
     *
     * @return the server brand
     */
    String getServerBrand();

    /**
     * Gets the version of the running platform
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Gets the plugins main data storage directory
     *
     * <p>Bukkit: ./plugins/MoonRise</p>
     * <p>Velocity: ./plugins/moonrise</p>
     * <p>Fabric: ./mods/MoonRise</p>
     *
     * @return the platforms data folder
     */
    Path getDataDirectory();

    /**
     * Gets the plugins configuration directory
     *
     * @return the config directory
     */
    default Path getConfigDirectory() {
        return this.getDataDirectory();
    }

    /**
     * Gets a bundled resource file from the jar
     *
     * @param path the path of the file
     * @return the file as an input stream
     */
    default InputStream getResourceStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    /**
     * Attempts to identify the plugin behind the given classloader.
     *
     * <p>Used for giving more helpful log messages when things break.</p>
     *
     * @param classLoader the classloader to identify
     * @return the name of the classloader source
     * @throws Exception anything
     */
    default @Nullable String identifyClassLoader(ClassLoader classLoader) throws Exception {
        return null;
    }
}