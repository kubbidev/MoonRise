package net.moonrise.api.event.extension;

import net.moonrise.api.event.MoonRiseEvent;
import net.moonrise.api.extension.Extension;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an {@link Extension} is loaded.
 */
public record ExtensionLoadEvent(Extension extension) implements MoonRiseEvent {

    /**
     * Gets the extension that was loaded.
     *
     * @return the extension
     */
    @Override
    public @NotNull Extension extension() {
        return this.extension;
    }
}
