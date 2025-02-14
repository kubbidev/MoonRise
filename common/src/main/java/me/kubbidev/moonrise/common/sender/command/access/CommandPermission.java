package me.kubbidev.moonrise.common.sender.command.access;

import me.kubbidev.moonrise.common.sender.Sender;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of the permissions required to execute built in MoonRise commands.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum CommandPermission {

    INFO("info"),
    RELOAD_CONFIG("reloadconfig"),
    TRANSLATIONS("translations");

    public static final String ROOT = "moonrise.";

    private final String permission;

    CommandPermission(String node) {
        this.permission = ROOT + node;
    }

    /**
     * Gets the permission string.
     *
     * @return the permission string
     */
    public @NotNull String getPermission() {
        return this.permission;
    }

    /**
     * Checks if a given {@link Sender} is authorized to execute the command.
     *
     * @param sender the command sender
     * @return true if the sender is authorized, false otherwise
     */
    public boolean isAuthorized(Sender sender) {
        return sender.hasPermission(this);
    }
}