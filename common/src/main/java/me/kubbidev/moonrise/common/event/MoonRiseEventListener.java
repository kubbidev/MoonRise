package me.kubbidev.moonrise.common.event;

import net.moonrise.api.event.EventBus;
import net.moonrise.api.event.MoonRiseEvent;

/**
 * Defines a class which listens to {@link MoonRiseEvent}s.
 */
public interface MoonRiseEventListener {

    void bind(EventBus bus);
}
