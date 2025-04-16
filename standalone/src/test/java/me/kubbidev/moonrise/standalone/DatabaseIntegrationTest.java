package me.kubbidev.moonrise.standalone;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.moonrise.api.platform.Health;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import me.kubbidev.moonrise.standalone.util.TestPluginBootstrap;
import me.kubbidev.moonrise.standalone.util.TestPluginProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseIntegrationTest {

    private static void testStorage(MoonRiseApplication app, TestPluginBootstrap bootstrap,
                                    TestPluginBootstrap.TestPlugin plugin) {
        // check the plugin is healthy
        Health health = plugin.runHealthCheck();
        assertNotNull(health);
        assertTrue(health.isHealthy());
    }

    @Nested
    class FlatFileDatabase {

        @Test
        public void testH2(@TempDir Path tempDir) {
            TestPluginProvider.use(tempDir, ImmutableMap.of("storage-method", "h2"),
                DatabaseIntegrationTest::testStorage);
        }

        @Test
        public void testSqlite(@TempDir Path tempDir) {
            TestPluginProvider.use(tempDir, ImmutableMap.of("storage-method", "sqlite"),
                DatabaseIntegrationTest::testStorage);
        }
    }
}
