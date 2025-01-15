package me.kubbidev.moonrise.common.sender.commands;

import me.kubbidev.moonrise.common.sender.command.abstraction.SingleCommand;
import me.kubbidev.moonrise.common.sender.command.access.CommandPermission;
import me.kubbidev.moonrise.common.sender.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.Predicates;

public class InfoCommand extends SingleCommand {
    public InfoCommand() {
        super(CommandSpec.INFO, "Info", CommandPermission.INFO, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        Message.INFO.send(sender, plugin, plugin.getStorage().getMeta());
    }
}
