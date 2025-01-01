package me.kubbidev.moonrise.standalone.app.integration;

import me.kubbidev.moonrise.api.util.Tristate;
import net.kyori.adventure.text.Component;

import java.util.Locale;
import java.util.UUID;

public interface StandaloneSender {
    String getName();

    UUID getUniqueId();

    void sendMessage(Component component);

    Tristate getPermissionValue(String permission);

    boolean hasPermission(String permission);

    boolean isConsole();

    Locale getLocale();
}