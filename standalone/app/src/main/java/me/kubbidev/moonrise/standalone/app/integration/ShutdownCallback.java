package me.kubbidev.moonrise.standalone.app.integration;

/**
 * Shutdown callback for the whole standalone app.
 * <p>
 * (in practice this is always implemented by the StandaloneLoader class)
 */
@FunctionalInterface
public interface ShutdownCallback {

    void shutdown();
}