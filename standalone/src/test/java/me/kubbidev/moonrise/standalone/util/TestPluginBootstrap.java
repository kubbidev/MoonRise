package me.kubbidev.moonrise.standalone.util;

import me.kubbidev.moonrise.common.storage.StorageType;
import me.kubbidev.moonrise.common.dependencies.Dependency;
import me.kubbidev.moonrise.common.dependencies.DependencyManager;
import me.kubbidev.moonrise.common.plugin.classpath.ClassPathAppender;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.standalone.MStandaloneBootstrap;
import me.kubbidev.moonrise.standalone.MStandalonePlugin;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneSender;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneUser;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

/**
 * An extension standalone bootstrap for testing.
 *
 * <p>Key differences:</p>
 * <p>
 * <ul>
 *     <li>Dependency loading system is replaced with a no-op stub that delegates to the test classloader</li>
 *     <li>Ability to register additional sender instances as being online</li>
 * </ul>
 * </p>
 */
public final class TestPluginBootstrap extends MStandaloneBootstrap {

    private static final ClassPathAppender NOOP_APPENDER = file -> {
    };

    private final Path       dataDirectory;
    private       TestPlugin plugin;

    public TestPluginBootstrap(MoonRiseApplication app, Path dataDirectory) {
        super(app, NOOP_APPENDER);
        this.dataDirectory = dataDirectory;
    }

    public TestPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    protected MStandalonePlugin createTestPlugin() {
        this.plugin = new TestPlugin(this);
        return this.plugin;
    }

    public static final class TestPlugin extends MStandalonePlugin {

        private final Set<StandaloneSender> onlineSenders = new CopyOnWriteArraySet<>();

        TestPlugin(MStandaloneBootstrap bootstrap) {
            super(bootstrap);
        }

        @Override
        protected DependencyManager createDependencyManager() {
            return new TestDependencyManager();
        }

        @Override
        public Stream<Sender> getOnlineSenders() {
            return Stream.concat(
                Stream.of(StandaloneUser.INSTANCE),
                this.onlineSenders.stream()
            ).map(sender -> getSenderFactory().wrap(sender));
        }

        public void addOnlineSender(StandaloneSender player) {
            this.onlineSenders.add(player);
        }
    }

    static final class TestDependencyManager implements DependencyManager {

        @Override
        public void loadDependencies(Set<Dependency> dependencies) {

        }

        @Override
        public void loadStorageDependencies(StorageType storageType) {

        }

        @Override
        public ClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
            return getClass().getClassLoader();
        }

        @Override
        public void close() {

        }
    }
}
