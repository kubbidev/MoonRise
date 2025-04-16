package me.kubbidev.moonrise.standalone.util;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class TestPluginProvider {

    private TestPluginProvider() {
    }

    /**
     * Creates a test MoonRise plugin instance, loads/enables it, and returns it.
     *
     * @param tempDir the temporary directory to run the plugin in
     * @param config  the config to set
     * @return the plugin
     */
    public static Plugin create(Path tempDir, Map<String, String> config) {
        Map<String, String> props = new HashMap<>(config);
        props.putIfAbsent("auto-install-translations", "false");

        props.forEach((k, v) -> System.setProperty("moonrise." + k, v));

        MoonRiseApplication app = new MoonRiseApplication(() -> {
        });
        TestPluginBootstrap bootstrap = new TestPluginBootstrap(app, tempDir);

        bootstrap.onLoad();
        bootstrap.onEnable();

        props.keySet().forEach((k) -> System.clearProperty("moonrise." + k));
        return new Plugin(app, bootstrap, bootstrap.getPlugin());
    }

    /**
     * Creates a test MoonRise plugin instance, loads/enables it, and returns it.
     *
     * @param tempDir the temporary directory to run the plugin in
     * @return the plugin
     */
    public static Plugin create(Path tempDir) {
        return create(tempDir, ImmutableMap.of());
    }

    /**
     * Creates a test MoonRise plugin instance, loads/enables it, runs the consumer, then disables it.
     *
     * @param tempDir  the temporary directory to run the plugin in
     * @param config   the config to set
     * @param consumer the consumer
     * @param <E>      the exception class thrown by the consumer
     * @throws E exception
     */
    public static <E extends Throwable> void use(Path tempDir, Map<String, String> config, Consumer<E> consumer)
        throws E {
        try (Plugin plugin = create(tempDir, config)) {
            consumer.accept(plugin.app, plugin.bootstrap, plugin.plugin);
        }
    }

    /**
     * Creates a test MoonRise plugin instance, loads/enables it, runs the consumer, then disables it.
     *
     * @param tempDir  the temporary directory to run the plugin in
     * @param consumer the consumer
     * @param <E>      the exception class thrown by the consumer
     * @throws E exception
     */
    public static <E extends Throwable> void use(Path tempDir, Consumer<E> consumer) throws E {
        use(tempDir, ImmutableMap.of(), consumer);
    }

    @FunctionalInterface
    public interface Consumer<E extends Throwable> {

        void accept(MoonRiseApplication app, TestPluginBootstrap bootstrap, TestPluginBootstrap.TestPlugin plugin)
            throws E;
    }

    public record Plugin(
        MoonRiseApplication app,
        TestPluginBootstrap bootstrap,
        TestPluginBootstrap.TestPlugin plugin
    ) implements AutoCloseable {

        @Override
        public void close() {
            this.bootstrap.onDisable();
        }
    }
}
