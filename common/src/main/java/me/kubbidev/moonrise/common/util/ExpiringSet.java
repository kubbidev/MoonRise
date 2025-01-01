package me.kubbidev.moonrise.common.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExpiringSet {
    private ExpiringSet() {}

    /**
     * An expiring set using Caffeine caches
     *
     * @param <E> the element type
     * @return a new expiring set
     */
    public static <E> Set<E> newExpiringSet(long duration, TimeUnit unit) {
        return Collections.newSetFromMap(CaffeineFactory.newBuilder().expireAfterWrite(duration, unit).<E, Boolean>build().asMap());
    }
}