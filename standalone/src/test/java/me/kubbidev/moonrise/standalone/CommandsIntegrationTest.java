package me.kubbidev.moonrise.standalone;

import com.google.common.collect.ImmutableMap;
import me.kubbidev.moonrise.common.sender.command.access.BuiltinPermission;
import me.kubbidev.moonrise.standalone.app.integration.CommandExecutor;
import me.kubbidev.moonrise.standalone.util.CommandTester;
import me.kubbidev.moonrise.standalone.util.TestPluginProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

public class CommandsIntegrationTest {

    private static final Map<String, String> CONFIG = ImmutableMap.<String, String>builder()
        .put("commands-rate-limit", "false")
        .build();

    @Test
    public void testNoPermissions(@TempDir Path tempDir) {
        TestPluginProvider.use(tempDir, CONFIG, (app, bootstrap, plugin) -> {
            CommandExecutor executor = app.getCommandExecutor();
            String version = "v" + bootstrap.getVersion();

            new CommandTester(executor)
                .givenHasPermissions(BuiltinPermission.HELP.getPermission())

                .whenRunCommand("")
                .thenExpect("""
                    [MR] Running MoonRise %s.
                    [MR] Use /m help to view available commands.
                    """.formatted(version))

                .whenRunCommand("help")
                .thenExpect("""
                    [MR] Sub Commands: (/ ...)
                    > help - [commands...]
                    """)

                .givenHasPermissions(/* empty */)

                .whenRunCommand("translations install")
                .thenExpect("[MR] Running MoonRise %s.".formatted(version));
        });
    }
}
