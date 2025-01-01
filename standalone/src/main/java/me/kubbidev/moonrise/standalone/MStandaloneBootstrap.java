package me.kubbidev.moonrise.standalone;

import me.kubbidev.api.platform.Platform;
import me.kubbidev.moonrise.common.loader.LoaderBootstrap;
import me.kubbidev.moonrise.common.plugin.bootstrap.BootstrappedWithLoader;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.plugin.classpath.ClassPathAppender;
import me.kubbidev.moonrise.common.plugin.classpath.JarInJarClassPathAppender;
import me.kubbidev.moonrise.common.plugin.logging.Log4jPluginLogger;
import me.kubbidev.moonrise.common.plugin.logging.PluginLogger;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

/**
 * Bootstrap plugin for MoonRise running as a standalone app.
 */
public class MStandaloneBootstrap implements MoonRiseBootstrap, LoaderBootstrap, BootstrappedWithLoader {
    private final MoonRiseApplication loader;

    private final PluginLogger logger;
    private final StandaloneSchedulerAdapter schedulerAdapter;
    private final ClassPathAppender classPathAppender;
    private final MStandalonePlugin plugin;

    private Instant startTime;
    private final CountDownLatch loadLatch = new CountDownLatch(1);
    private final CountDownLatch enableLatch = new CountDownLatch(1);

    public MStandaloneBootstrap(MoonRiseApplication loader) {
        this.loader = loader;

        this.logger = new Log4jPluginLogger(MoonRiseApplication.LOGGER);
        this.schedulerAdapter = new StandaloneSchedulerAdapter(this);
        this.classPathAppender = new JarInJarClassPathAppender(getClass().getClassLoader());
        this.plugin = new MStandalonePlugin(this);
    }

    @VisibleForTesting
    protected MStandaloneBootstrap(MoonRiseApplication loader, ClassPathAppender classPathAppender) {
        this.loader = loader;

        this.logger = new Log4jPluginLogger(MoonRiseApplication.LOGGER);
        this.schedulerAdapter = new StandaloneSchedulerAdapter(this);
        this.classPathAppender = classPathAppender;
        this.plugin = createTestPlugin();
    }

    @VisibleForTesting
    protected MStandalonePlugin createTestPlugin() {
        return new MStandalonePlugin(this);
    }

    // provide adapters

    @Override
    public MoonRiseApplication getLoader() {
        return this.loader;
    }

    @Override
    public PluginLogger getPluginLogger() {
        return this.logger;
    }

    @Override
    public SchedulerAdapter getScheduler() {
        return this.schedulerAdapter;
    }

    @Override
    public ClassPathAppender getClassPathAppender() {
        return this.classPathAppender;
    }

    // lifecycle

    @Override
    public void onLoad() {
        try {
            this.plugin.load();
        } finally {
            this.loadLatch.countDown();
        }
    }

    @Override
    public void onEnable() {
        this.startTime = Instant.now();
        try {
            this.plugin.enable();
        } finally {
            this.enableLatch.countDown();
        }
    }

    @Override
    public void onDisable() {
        this.plugin.disable();
    }

    @Override
    public CountDownLatch getLoadLatch() {
        return this.loadLatch;
    }

    @Override
    public CountDownLatch getEnableLatch() {
        return this.enableLatch;
    }

    // provide information about the plugin

    @Override
    public String getVersion() {
        return this.loader.getVersion();
    }

    @Override
    public Instant getStartupTime() {
        return this.startTime;
    }

    // provide information about the platform


    @Override
    public Platform.Type getType() {
        return Platform.Type.STANDALONE;
    }

    @Override
    public String getServerBrand() {
        return "standalone";
    }

    @Override
    public String getServerVersion() {
        return "n/a";
    }

    @Override
    public Path getDataDirectory() {
        return Paths.get("data").toAbsolutePath();
    }
}
