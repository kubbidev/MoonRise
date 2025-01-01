package me.kubbidev.moonrise.standalone;

import net.moonrise.api.util.Tristate;
import me.kubbidev.moonrise.common.sender.SenderFactory;
import me.kubbidev.moonrise.common.locale.TranslationManager;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class StandaloneSenderFactory extends SenderFactory<MStandalonePlugin, StandaloneSender> {

    public StandaloneSenderFactory(MStandalonePlugin plugin) {
        super(plugin);
    }

    @Override
    protected String getName(StandaloneSender sender) {
        return sender.getName();
    }

    @Override
    protected UUID getUniqueId(StandaloneSender sender) {
        return sender.getUniqueId();
    }

    @Override
    protected void sendMessage(StandaloneSender sender, Component message) {
        Component rendered = TranslationManager.render(message, sender.getLocale());
        sender.sendMessage(rendered);
    }

    @Override
    protected Tristate getPermissionValue(StandaloneSender sender, String node) {
        return sender.getPermissionValue(node);
    }

    @Override
    protected boolean hasPermission(StandaloneSender sender, String node) {
        return sender.hasPermission(node);
    }

    @Override
    protected boolean isConsole(StandaloneSender sender) {
        return sender.isConsole();
    }

    @Override
    protected boolean shouldSplitNewlines(StandaloneSender sender) {
        return true;
    }
}