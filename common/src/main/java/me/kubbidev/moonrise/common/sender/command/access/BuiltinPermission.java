package me.kubbidev.moonrise.common.sender.command.access;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of the permissions required to execute built in MoonRise commands.
 */
@SuppressWarnings("SpellCheckingInspection")
public enum BuiltinPermission implements CommandPermission {

    HELP("help"),
    INFO("info"),
    RELOAD_CONFIG("reloadconfig"),
    TRANSLATIONS("translations");

    public static final String ROOT = "moonrise.";

    private final String permission;

    BuiltinPermission(String node) {
        this.permission = ROOT + node;
    }

    /**
     * Gets the permission string.
     *
     * @return the permission string
     */
    @Override
    public @NotNull String getPermission() {
        return this.permission;
    }
}
