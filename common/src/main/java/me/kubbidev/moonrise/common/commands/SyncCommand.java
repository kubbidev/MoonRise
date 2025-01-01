package me.kubbidev.moonrise.common.commands;

import me.kubbidev.moonrise.common.command.abstraction.SingleCommand;
import me.kubbidev.moonrise.common.command.access.CommandPermission;
import me.kubbidev.moonrise.common.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.command.util.ArgumentList;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.Predicates;

public class SyncCommand extends SingleCommand {
    public SyncCommand() {
        super(CommandSpec.SYNC, "Sync", CommandPermission.SYNC, Predicates.alwaysFalse());
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        Message.UPDATE_TASK_REQUEST.send(sender);
        plugin.getSyncTaskBuffer().request().join();
        Message.UPDATE_TASK_COMPLETE.send(sender);
    }
}
