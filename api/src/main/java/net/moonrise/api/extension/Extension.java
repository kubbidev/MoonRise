package net.moonrise.api.extension;

import net.moonrise.api.MoonRise;

/**
 * Represents a simple extension "plugin" for MoonRise.
 *
 * <p>Yes, that's right. A plugin for a plugin.</p>
 *
 * <p>Extensions should either declare a no-arg constructor, or a constructor
 * that accepts a single {@link MoonRise} parameter as it's only argument.</p>
 */
public interface Extension {

    /**
     * Loads the extension.
     */
    void load();

    /**
     * Unloads the extension.
     */
    void unload();
}