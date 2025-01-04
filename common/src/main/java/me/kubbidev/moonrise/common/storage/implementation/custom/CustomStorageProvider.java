package me.kubbidev.moonrise.common.storage.implementation.custom;

import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

/**
 * A storage provider
 */
@FunctionalInterface
public interface CustomStorageProvider {

    StorageImplementation provide(MoonRisePlugin plugin);
}