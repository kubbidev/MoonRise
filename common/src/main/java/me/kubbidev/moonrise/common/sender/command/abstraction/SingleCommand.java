package me.kubbidev.moonrise.common.sender.command.abstraction;

import me.kubbidev.moonrise.common.sender.command.access.CommandPermission;
import me.kubbidev.moonrise.common.sender.command.spec.CommandSpec;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Represents a single "main" command (one without any children)
 */
public abstract class SingleCommand extends ChildCommand<Void> {

    public SingleCommand(CommandSpec spec, String name, @Nullable CommandPermission permission, Predicate<Integer> argumentCheck) {
        super(spec, name, permission, argumentCheck);
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, Void ignored, ArgumentList args, String label) throws CommandException {
        execute(plugin, sender, args, label);
    }

    public abstract void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException;
}