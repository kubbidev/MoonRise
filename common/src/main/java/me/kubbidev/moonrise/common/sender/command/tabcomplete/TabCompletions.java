package me.kubbidev.moonrise.common.sender.command.tabcomplete;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

/**
 * Common completion suppliers used by the plugin
 */
public final class TabCompletions {

    private static final CompletionSupplier BOOLEAN = CompletionSupplier.startsWith("true", "false");

    public TabCompletions(MoonRisePlugin plugin) {
    }

    // bit of a weird pattern, but meh it kinda works, reduces the boilerplate
    // of calling the command manager + tab completions getters every time

    public static CompletionSupplier booleans() {
        return BOOLEAN;
    }
}