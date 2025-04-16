package me.kubbidev.moonrise.common.sender.command.access;

import java.util.function.Supplier;
import me.kubbidev.moonrise.common.sender.Sender;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command permission check.
 */
@FunctionalInterface
public interface CommandPermission {

    /**
     * Creates a CommandPermission instance with a static permission string.
     *
     * @param permission the permission string
     * @return a CommandPermission instance
     */
    static CommandPermission of(String permission) {
        return () -> permission;
    }

    /**
     * Creates a CommandPermission instance with a dynamic permission {@link Supplier}.
     *
     * @param permission a supplier providing the permission string
     * @return a CommandPermission instance
     */
    static CommandPermission of(Supplier<String> permission) {
        return permission::get;
    }

    /**
     * Gets the permission string.
     *
     * @return the permission string
     */
    @NotNull String getPermission();

    /**
     * Checks if a given {@link Sender} is authorized to execute the command.
     *
     * @param sender the command sender
     * @return true if the sender is authorized, false otherwise
     */
    default boolean isAuthorized(Sender sender) {
        return sender.hasPermission(this);
    }
}