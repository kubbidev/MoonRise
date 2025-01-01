package me.kubbidev.moonrise.common.plugin.util;

import com.google.gson.Gson;
import net.moonrise.api.platform.Health;

import java.util.Map;

public record HealthCheckResult(boolean isHealthy, Map<String, Object> getDetails) implements Health {
    private static final Gson GSON = new Gson();

    public static HealthCheckResult healthy(Map<String, Object> details) {
        return new HealthCheckResult(true, details);
    }

    public static HealthCheckResult unhealthy(Map<String, Object> details) {
        return new HealthCheckResult(false, details);
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}