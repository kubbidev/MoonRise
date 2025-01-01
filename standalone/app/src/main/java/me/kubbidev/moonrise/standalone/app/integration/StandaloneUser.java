package me.kubbidev.moonrise.standalone.app.integration;

import me.kubbidev.api.util.Tristate;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;

import java.util.Locale;
import java.util.UUID;

/**
 * The sender instance used for the console / users executing commands
 * on a standalone instance of MoonRise
 */
public class StandaloneUser implements StandaloneSender {

    private static final UUID UUID = new UUID(0, 0);

    public static final StandaloneUser INSTANCE = new StandaloneUser();

    private StandaloneUser() {
    }

    @Override
    public String getName() {
        return "StandaloneUser";
    }

    @Override
    public UUID getUniqueId() {
        return UUID;
    }

    @Override
    public void sendMessage(Component component) {
        MoonRiseApplication.LOGGER.info(ANSIComponentSerializer.ansi().serialize(component));
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return Tristate.TRUE;
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public boolean isConsole() {
        return true;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }
}