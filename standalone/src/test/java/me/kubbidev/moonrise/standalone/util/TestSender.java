package me.kubbidev.moonrise.standalone.util;

import net.moonrise.api.util.Tristate;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneSender;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneUser;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;

public class TestSender implements StandaloneSender {
    private final Set<Consumer<Component>> messageSinks;

    private String name = "StandaloneUser";
    private UUID uniqueId = UUID.randomUUID();
    private boolean isConsole = false;

    private Function<String, Tristate> permissionChecker;

    public TestSender() {
        this.messageSinks = new CopyOnWriteArraySet<>();
        this.messageSinks.add(StandaloneUser.INSTANCE::sendMessage);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(UUID uuid) {
        this.uniqueId = uuid;
    }

    @Override
    public void sendMessage(Component component) {
        for (Consumer<Component> sink : this.messageSinks) {
            sink.accept(component);
        }
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return this.permissionChecker == null
                ? Tristate.TRUE
                : this.permissionChecker.apply(permission);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.getPermissionValue(permission).asBoolean();
    }

    @Override
    public boolean isConsole() {
        return this.isConsole;
    }

    public void setConsole(boolean console) {
        this.isConsole = console;
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    public void setPermissionChecker(Function<String, Tristate> permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    public void addMessageSink(Consumer<Component> sink) {
        this.messageSinks.add(sink);
    }
}
