package net.moonrise.api.platform;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Provides information about the platform MoonRise is running on.
 */
public interface Platform {

    /**
     * Gets the type of platform MoonRise is running on
     *
     * @return the type of platform MoonRise is running on
     */
    Platform.@NotNull Type getType();

    /**
     * Gets the time when the plugin first started.
     *
     * @return the enable time
     */
    @NotNull Instant getStartTime();

    /**
     * Represents a type of platform which MoonRise can run on.
     */
    enum Type {
        STANDALONE("Standalone");

        private final String friendlyName;

        Type(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        /**
         * Gets a readable name for the platform type.
         *
         * @return a readable name
         */
        public @NotNull String getFriendlyName() {
            return this.friendlyName;
        }
    }
}