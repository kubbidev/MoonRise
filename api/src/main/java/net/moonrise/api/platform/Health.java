package net.moonrise.api.platform;

import java.util.Map;

/**
 * Represents the "health" status (healthcheck) of a MoonRise implementation.
 */
public interface Health {

    /**
     * Gets if MoonRise is healthy.
     *
     * @return if MoonRise is healthy
     */
    boolean isHealthy();

    /**
     * Gets extra metadata/details about the healthcheck result.
     *
     * @return details about the healthcheck status
     */
    Map<String, Object> getDetails();
}