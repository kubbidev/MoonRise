package me.kubbidev.moonrise.common.sender.commands;

import me.kubbidev.moonrise.common.sender.command.abstraction.Command;
import me.kubbidev.moonrise.common.sender.command.access.BuiltinPermission;
import me.kubbidev.moonrise.common.sender.command.spec.BuildinDefinition;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.Predicates;

public class ReloadConfigCommand extends Command {

    public ReloadConfigCommand() {
        super("ReloadConfig",
            BuildinDefinition.RELOAD_CONFIG,
            BuiltinPermission.RELOAD_CONFIG, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        plugin.getConfiguration().reload();
        Message.RELOAD_CONFIG_SUCCESS.send(sender);
    }
}
