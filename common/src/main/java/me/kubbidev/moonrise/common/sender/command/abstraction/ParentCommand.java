package me.kubbidev.moonrise.common.sender.command.abstraction;

import java.util.Optional;
import me.kubbidev.moonrise.common.sender.command.spec.CommandDefinition;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.CompletionSupplier;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.TabCompleter;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import me.kubbidev.moonrise.common.util.Predicates;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class ParentCommand extends Command {

    private final List<Command> children = new ArrayList<>();

    public ParentCommand(String name, CommandDefinition definition) {
        super(name, definition, null, Predicates.alwaysFalse());
    }

    public boolean addChildren(Command child) {
        return this.children.add(child);
    }

    public boolean removeChildren(Command child) {
        return this.children.remove(child);
    }

    public @NotNull List<Command> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        // check if required argument and/or subcommand is missing
        if (args.isEmpty()) {
            this.sendChildrenUsage(sender, label);
            return;
        }

        List<Command> subs = this.children.stream()
            .filter(s -> s.getName().equalsIgnoreCase(args.getFirst()))
            .collect(ImmutableCollectors.toList());

        if (subs.isEmpty()) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return;
        }

        boolean authorize = false;
        Optional<Command> sub;
        if (subs.size() == 1) {
            sub = Optional.of(subs.getFirst());
        } else {
            authorize = true;
            sub = subs.stream()
                .filter(c -> c.hasPermission(sender))
                .findFirst(); // maybe more than one
        }

        if (sub.isEmpty()) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return;
        } else {
            if (!(authorize || sub.get().hasPermission(sender))) {
                Message.COMMAND_NO_PERMISSION.send(sender);
                return;
            }
        }

        String concatLabel = String.format("%s %s", label, args.getFirst());
        if (sub.get().getArgumentCheck().test(args.size() - 1)) {
            sub.get().sendDetailedUsage(sender, concatLabel);
            return;
        }

        try {
            sub.get().execute(plugin, sender, args.subList(1, args.size()), concatLabel);
        } catch (CommandException e) {
            e.handle(sender, concatLabel, sub.get());
        }
    }

    @Override
    public List<String> tabComplete(MoonRisePlugin plugin, Sender sender, ArgumentList args) {
        return TabCompleter.create()
            .at(0, CompletionSupplier.startsWith(() -> this.children.stream()
                .filter(s -> s.shouldDisplay(sender))
                .filter(s -> s.hasPermission(sender))
                .map(s -> s.getName().toLowerCase(Locale.ROOT))
            ))
            .from(1, partial -> this.children.stream()
                .filter(s -> s.shouldDisplay(sender))
                .filter(s -> s.hasPermission(sender))
                .filter(s -> s.getName().equalsIgnoreCase(args.getFirst()))
                .findFirst()
                .map(cmd -> cmd.tabComplete(plugin, sender, args.subList(1, args.size())))
                .orElse(Collections.emptyList())
            )
            .complete(args);
    }

    private void sendChildrenUsage(Sender sender, String label) {
        List<Command> subs = this.children.stream()
            .filter(s -> s.shouldDisplay(sender))
            .filter(s -> s.hasPermission(sender))
            .collect(ImmutableCollectors.toList());

        if (!subs.isEmpty()) {
            Message.MAIN_COMMAND_USAGE_HEADER.send(sender, label);
            for (Command s : subs) {
                s.sendUsage(sender, String.format("%s %s", label,
                    s.getName().toLowerCase(Locale.ROOT)
                ));
            }
        } else {
            Message.COMMAND_NO_PERMISSION.send(sender);
        }
    }

    @Override
    public boolean hasPermission(Sender sender) {
        return this.children.stream().anyMatch(c -> c.hasPermission(sender));
    }
}
