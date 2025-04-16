package me.kubbidev.moonrise.common.plugin.bootstrap;

/**
 * A {@link MoonRiseBootstrap} that was bootstrapped by a loader.
 */
@FunctionalInterface
public interface BootstrappedWithLoader {

    /**
     * Gets the loader object that did the bootstrapping.
     *
     * @return the loader
     */
    Object getLoader();
}