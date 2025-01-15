package me.kubbidev.moonrise.common.sender.commands;

import me.kubbidev.moonrise.common.sender.command.abstraction.SingleCommand;
import me.kubbidev.moonrise.common.sender.command.access.CommandPermission;
import me.kubbidev.moonrise.common.sender.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.Predicates;

public class ReloadConfigCommand extends SingleCommand {
    public ReloadConfigCommand() {
        super(CommandSpec.RELOAD_CONFIG, "ReloadConfig", CommandPermission.RELOAD_CONFIG, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        plugin.getConfiguration().reload();
        Message.RELOAD_CONFIG_SUCCESS.send(sender);
    }
}
