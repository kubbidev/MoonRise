package me.kubbidev.moonrise.common.event;

import me.kubbidev.moonrise.api.event.MoonRiseEvent;
import me.kubbidev.moonrise.api.event.sync.ConfigReloadEvent;
import me.kubbidev.moonrise.api.event.type.Cancellable;
import me.kubbidev.moonrise.api.event.type.ResultEvent;

public record EventDispatcher(AbstractEventBus<?> eventBus) {

    private void post(MoonRiseEvent event) {
        this.eventBus.post(event);
    }

    private void postAsync(MoonRiseEvent event) {
        Class<? extends MoonRiseEvent> eventClass = event.getClass();

        // check against common mistakes - events with any sort of result shouldn't be posted async
        if (Cancellable.class.isAssignableFrom(eventClass) || ResultEvent.class.isAssignableFrom(eventClass)) {
            throw new RuntimeException("Event cannot be posted async (" + eventClass.getName() + ")");
        }

        // if there aren't any handlers registered for the event, don't bother trying to post it
        if (!this.eventBus.shouldPost(eventClass)) {
            return;
        }

        // async: post it
        this.eventBus.getPlugin().getBootstrap().getScheduler().executeAsync(() -> post(event));
    }

    private void postSync(MoonRiseEvent event) {
        if (this.eventBus.shouldPost(event.getClass())) {
            this.post(event); // if there are any handlers registered for our event, try to post it
        }
    }

    private boolean postCancellable(MoonRiseEvent event) {
        return this.postCancellable(event, false);
    }

    private boolean postCancellable(MoonRiseEvent event, boolean initialState) {
        Class<? extends MoonRiseEvent> eventClass = event.getClass();
        if (!Cancellable.class.isAssignableFrom(eventClass)) {
            throw new RuntimeException("Event is not cancellable: " + eventClass.getName());
        }

        // if there aren't any handlers registered for the event, just return the initial state
        if (!this.eventBus.shouldPost(eventClass)) {
            return initialState;
        }

        Cancellable cancellable = (Cancellable) event;
        cancellable.setCancelled(initialState);
        this.post(event);

        // return the final status
        return cancellable.isCancelled();
    }

    public void dispatchConfigReload() {
        this.postAsync(new ConfigReloadEvent());
    }
}
