package me.kubbidev.moonrise.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Provides static access to the {@link MoonRise} API.
 *
 * <p>Ideally, the ServiceManager for the platform should be used to obtain an
 * instance, however, this provider can be used if this is not viable.</p>
 */
public final class MoonRiseProvider {

    private static MoonRise instance = null;

    /**
     * Gets an instance of the {@link MoonRise} API, throwing {@link IllegalStateException} if the API is not loaded
     * yet.
     *
     * <p>This method will never return null.</p>
     *
     * @return an instance of the MoonRise API
     * @throws IllegalStateException if the API is not loaded yet
     */
    public static @NotNull MoonRise get() {
        MoonRise instance = MoonRiseProvider.instance;
        if (instance == null) {
            throw new NotLoadedException();
        }
        return instance;
    }

    @ApiStatus.Internal
    static void register(MoonRise instance) {
        MoonRiseProvider.instance = instance;
    }

    @ApiStatus.Internal
    static void unregister() {
        MoonRiseProvider.instance = null;
    }

    @ApiStatus.Internal
    private MoonRiseProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Exception thrown when the API is requested before it has been loaded.
     */
    private static final class NotLoadedException extends IllegalStateException {

        private static final String MESSAGE = """
            The MoonRise API isn't loaded yet!
            This could be because:
              a) the MoonRise plugin is not installed or it failed to enable
              b) the plugin in the stacktrace does not declare a dependency on MoonRise
              c) the plugin in the stacktrace is retrieving the API before the plugin 'enable' phase
                 (call the #get method in onEnable, not the constructor!)
              d) the plugin in the stacktrace is incorrectly 'shading' the MoonRise API into its jar
            """;

        NotLoadedException() {
            super(MESSAGE);
        }
    }
}