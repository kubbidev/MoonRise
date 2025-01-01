package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.abstraction.SingleCommand;
import me.kubbidev.moonrise.common.command.access.CommandPermission;
import me.kubbidev.moonrise.common.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.command.util.ArgumentList;
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
        Message.INFO.send(sender, plugin, plugin.getDatabase().getMeta());
    }
}
