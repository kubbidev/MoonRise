package me.kubbidev.moonrise.standalone.util;

import me.kubbidev.moonrise.api.util.Tristate;
import me.kubbidev.moonrise.standalone.app.integration.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellij.lang.annotations.RegExp;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Utility for testing MoonRise commands with BDD-like given/when/then assertions.
 */
public final class CommandTester implements Consumer<Component>, Function<String, Tristate> {

    private static final Logger LOGGER = LogManager.getLogger(CommandTester.class);

    /**
     * The MoonRise command executor
     */
    private final CommandExecutor       executor;
    /**
     * The test player
     */
    private final TestSender            sender;
    /**
     * The current map of permissions held by the fake executor
     */
    private       Map<String, Tristate> permissions        = null;
    /**
     * A set of the permissions that have been checked for
     */
    private final Set<String>           checkedPermissions = Collections.synchronizedSet(new HashSet<>());
    /**
     * A buffer of messages received by the test tool
     */
    private final List<Component>       messageBuffer      = Collections.synchronizedList(new ArrayList<>());

    public CommandTester(CommandExecutor executor, TestSender sender) {
        this.executor = executor;
        this.sender = sender;

        this.sender.setPermissionChecker(this);
        this.sender.addMessageSink(this);
    }

    public CommandTester(CommandExecutor executor) {
        this(executor, new TestSender());
    }

    /**
     * Accept a message and add it to the buffer.
     *
     * @param component the message
     */
    @Override
    public void accept(Component component) {
        this.messageBuffer.add(component);
    }

    /**
     * Perform a permission check for the fake executor
     *
     * @param permission the permission
     * @return the result of the permission check
     */
    @Override
    public Tristate apply(String permission) {
        if (this.permissions == null) {
            this.checkedPermissions.add(permission);
            return Tristate.TRUE;
        } else {
            Tristate result = this.permissions.getOrDefault(permission, Tristate.UNDEFINED);
            if (result != Tristate.UNDEFINED) {
                this.checkedPermissions.add(permission);
            }
            return result;
        }
    }

    /**
     * Marks that the fake executor should have all permissions
     *
     * @return this
     */
    public CommandTester givenHasAllPermissions() {
        this.permissions = null;
        return this;
    }

    /**
     * Marks that the fake executor should have the given permissions
     *
     * @return this
     */
    public CommandTester givenHasPermissions(String... permissions) {
        this.permissions = new HashMap<>();
        for (String permission : permissions) {
            this.permissions.put(permission, Tristate.TRUE);
        }
        return this;
    }

    /**
     * Execute a command using the {@link CommandExecutor} and capture output to this test instance.
     *
     * @param command the command to run
     * @return this
     */
    public CommandTester whenRunCommand(String command) {
        LOGGER.info("Executing test command: {}", command);
        this.executor.execute(this.sender, command).join();
        return this;
    }

    /**
     * Asserts that the current contents of the message buffer matches the given input string.
     *
     * @param expected the expected contents
     * @return this
     */
    public CommandTester thenExpect(String expected) {
        String actual = this.renderBuffer();
        assertEquals(expected.trim(), actual.trim());

        if (this.permissions != null) {
            assertEquals(this.checkedPermissions, this.permissions.keySet());
        }

        return this.clearMessageBuffer();
    }

    /**
     * Asserts that the current contents of the message buffer starts with the given input string.
     *
     * @param expected the expected contents
     * @return this
     */
    public CommandTester thenExpectStartsWith(String expected) {
        String actual = this.renderBuffer();
        assertTrue(actual.trim().startsWith(expected.trim()),
            "expected '" + actual + "' to start with '" + expected + "'");

        if (this.permissions != null) {
            assertEquals(this.checkedPermissions, this.permissions.keySet());
        }

        return this.clearMessageBuffer();
    }

    /**
     * Asserts that the current contents of the message buffer matches the given input string.
     *
     * @param expected the expected contents
     * @return this
     */
    public CommandTester thenExpectReplacing(@RegExp String regex, String replacement, String expected) {
        String actual = this.renderBuffer().replaceAll(regex, replacement);
        assertEquals(expected.trim(), actual.trim());

        if (this.permissions != null) {
            assertEquals(this.checkedPermissions, this.permissions.keySet());
        }

        return this.clearMessageBuffer();
    }

    /**
     * Clears the message buffer.
     *
     * @return this
     */
    public CommandTester clearMessageBuffer() {
        this.messageBuffer.clear();
        this.checkedPermissions.clear();
        return this;
    }

    /**
     * Renders the contents of the message buffer as a stream of lines.
     *
     * @return rendered copy of the buffer
     */
    public Stream<String> renderBufferStream() {
        return this.messageBuffer.stream().map(c -> PlainTextComponentSerializer.plainText().serialize(c));
    }

    /**
     * Renders the contents of the message buffer as a joined string.
     *
     * @return rendered copy of the buffer
     */
    public String renderBuffer() {
        return this.renderBufferStream().map(String::trim).collect(Collectors.joining("\n"));
    }

    /**
     * Prints test case source code to stdout to test the given command.
     *
     * @param cmd the command
     * @return this
     */
    public CommandTester outputTest(String cmd) {
        this.whenRunCommand(cmd);

        String checkedPermissions = this.checkedPermissions.stream()
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(", "));

        System.out.printf(".givenHasPermissions(%s)%n", checkedPermissions);
        System.out.printf(".whenRunCommand(\"%s\")%n", cmd);

        List<String> render = this.renderBufferStream().toList();
        if (render.size() == 1) {
            System.out.printf(".thenExpect(\"%s\")%n", render.getFirst());
        } else {
            System.out.println(".thenExpect(\"\"\"");
            for (String s : render) {
                System.out.println("        " + s);
            }
            System.out.println("        \"\"\"");
            System.out.println(")");
        }

        System.out.println();
        return this.clearMessageBuffer();
    }
}
