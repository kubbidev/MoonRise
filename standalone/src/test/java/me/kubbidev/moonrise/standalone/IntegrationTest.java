package me.kubbidev.moonrise.standalone;

import me.kubbidev.moonrise.api.platform.Health;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.standalone.util.TestPluginProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A set of 'integration tests' for the standalone MoonRise app.
 */
public class IntegrationTest {

    @Test
    public void testLoadEnableDisable(@TempDir Path tempDir) {
        TestPluginProvider.use(tempDir, (app, bootstrap, plugin) -> {
            Health health = plugin.runHealthCheck();
            assertNotNull(health);
            assertTrue(health.isHealthy());
        });
    }

    @Test
    public void testReloadConfig(@TempDir Path tempDir) throws IOException {
        TestPluginProvider.use(tempDir, (app, bootstrap, plugin) -> {
            String token = plugin.getConfiguration().get(ConfigKeys.AUTHENTICATION_TOKEN);
            assertEquals("", token);

            Path config = tempDir.resolve("config.yml");
            assertTrue(Files.exists(config));

            String configString = Files.readString(config)
                    .replace("authentication-token: ''", "authentication-token: 'TOKEN'");

            Files.writeString(config, configString);
            plugin.getConfiguration().reload();

            token = plugin.getConfiguration().get(ConfigKeys.AUTHENTICATION_TOKEN);
            assertEquals("", token); // unchanged
        });
    }
}