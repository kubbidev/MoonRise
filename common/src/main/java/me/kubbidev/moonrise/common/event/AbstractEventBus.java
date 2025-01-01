package me.kubbidev.moonrise.common.event;

import net.moonrise.api.event.EventBus;
import net.moonrise.api.event.EventSubscription;
import net.moonrise.api.event.MoonRiseEvent;
import me.kubbidev.moonrise.common.api.MoonRiseApiProvider;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import net.kyori.event.EventSubscriber;
import net.kyori.event.SimpleEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractEventBus<P> implements EventBus, AutoCloseable {

    /**
     * The plugin instance
     */
    private final MoonRisePlugin plugin;

    /**
     * The api provider instance
     */
    private final MoonRiseApiProvider apiProvider;

    /**
     * The delegate event bus
     */
    private final Bus bus = new Bus();

    public AbstractEventBus(MoonRisePlugin plugin, MoonRiseApiProvider apiProvider) {
        this.plugin = plugin;
        this.apiProvider = apiProvider;
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    public MoonRiseApiProvider getApiProvider() {
        return this.apiProvider;
    }

    /**
     * Checks that the given plugin object is a valid plugin instance for the platform
     *
     * @param plugin the object
     * @return a plugin
     * @throws IllegalArgumentException if the plugin is invalid
     */
    protected abstract P checkPlugin(Object plugin) throws IllegalArgumentException;

    public void post(MoonRiseEvent event) {
        this.bus.post(event);
    }

    public boolean shouldPost(Class<? extends MoonRiseEvent> eventClass) {
        return this.bus.hasSubscribers(eventClass);
    }

    public void subscribe(MoonRiseEventListener listener) {
        listener.bind(this);
    }

    @Override
    public @NotNull <T extends MoonRiseEvent> EventSubscription<T> subscribe(@NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler) {
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(handler, "handler");
        return registerSubscription(eventClass, handler, null);
    }

    @Override
    public @NotNull <T extends MoonRiseEvent> EventSubscription<T> subscribe(Object plugin, @NotNull Class<T> eventClass, @NotNull Consumer<? super T> handler) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(eventClass, "eventClass");
        Objects.requireNonNull(handler, "handler");
        return registerSubscription(eventClass, handler, checkPlugin(plugin));
    }

    private <T extends MoonRiseEvent> EventSubscription<T> registerSubscription(Class<T> eventClass, Consumer<? super T> handler, Object plugin) {
        if (!MoonRiseEvent.class.isAssignableFrom(eventClass)) {
            throw new IllegalArgumentException("class " + eventClass.getName() + " does not implement MoonRiseEvent");
        }

        MoonRiseEventSubscription<T> eventHandler = new MoonRiseEventSubscription<>(this, eventClass, handler, plugin);
        this.bus.register(eventClass, eventHandler);

        return eventHandler;
    }

    @Override
    public @NotNull @Unmodifiable <T extends MoonRiseEvent> Set<EventSubscription<T>> getSubscriptions(@NotNull Class<T> eventClass) {
        return this.bus.getHandlers(eventClass);
    }

    /**
     * Removes a specific handler from the bus
     *
     * @param handler the handler to remove
     */
    public void unregisterHandler(MoonRiseEventSubscription<?> handler) {
        this.bus.unregister(handler);
    }

    /**
     * Removes all handlers for a specific plugin
     *
     * @param plugin the plugin
     */
    protected void unregisterHandlers(P plugin) {
        this.bus.unregister(sub -> ((MoonRiseEventSubscription<?>) sub).getPlugin() == plugin);
    }

    @Override
    public void close() {
        this.bus.unregisterAll();
    }

    private static final class Bus extends SimpleEventBus<MoonRiseEvent> {
        public Bus() {
            super(MoonRiseEvent.class);
        }

        @Override
        protected boolean shouldPost(@NotNull MoonRiseEvent event, @NotNull EventSubscriber<?> subscriber) {
            return true;
        }

        public <T extends MoonRiseEvent> Set<EventSubscription<T>> getHandlers(Class<T> eventClass) {
            //noinspection unchecked
            return super.subscribers().values().stream()
                    .filter(s -> s instanceof EventSubscription && ((EventSubscription<?>) s).getEventClass().isAssignableFrom(eventClass))
                    .map(s -> (EventSubscription<T>) s)
                    .collect(Collectors.toSet());
        }
    }
}
