package me.kubbidev.moonrise.common.database.implementation.custom;

/**
 * Hook to allow external code to provide a database implementation
 */
public final class CustomDatabaseProviders {
    private CustomDatabaseProviders() {}

    private static CustomDatabaseProvider provider = null;

    public static void register(CustomDatabaseProvider provider) {
        CustomDatabaseProviders.provider = provider;
    }

    public static CustomDatabaseProvider getProvider() {
        if (CustomDatabaseProviders.provider == null) {
            throw new IllegalStateException("Provider not present.");
        }

        return CustomDatabaseProviders.provider;
    }
}
