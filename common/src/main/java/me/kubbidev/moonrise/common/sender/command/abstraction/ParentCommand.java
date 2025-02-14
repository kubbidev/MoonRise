package me.kubbidev.moonrise.common.sender.command.abstraction;

import me.kubbidev.moonrise.common.sender.command.spec.CommandSpec;
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

public abstract class ParentCommand<T, I> extends Command<Void> {
    /** The type of parent command */
    private final Type type;
    private final List<Command<T>> children = new ArrayList<>();

    public ParentCommand(CommandSpec spec, String name, Type type) {
        super(spec, name, null, Predicates.alwaysFalse());
        this.type = type;
    }

    public boolean addChildren(Command<T> child) {
        return this.children.add(child);
    }

    public boolean removeChildren(Command<T> child) {
        return this.children.remove(child);
    }

    public @NotNull List<Command<T>> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, Void ignored, ArgumentList args, String label) throws CommandException {
        // check if required argument and/or subcommand is missing
        if (args.size() < this.type.minArgs) {
            this.sendUsage(sender, label);
            return;
        }

        List<Command<T>> subs = this.children.stream()
                .filter(s -> s.getName().equalsIgnoreCase(args.get(this.type.index)))
                .collect(ImmutableCollectors.toList());

        if (subs.isEmpty()) {
            Message.COMMAND_NOT_RECOGNISED.send(sender);
            return;
        }

        boolean authorize = false;
        Command<T> sub;
        if (subs.size() == 1) {
            sub = subs.getFirst();
        } else {
            authorize = true;
            sub = subs.stream()
                    .filter(c -> c.isAuthorized(sender))
                    .findFirst() // maybe more than one
                    .orElse(null);
        }

        if (sub == null) {
            Message.COMMAND_NO_PERMISSION.send(sender);
            return;
        } else {
            if (!(authorize || sub.isAuthorized(sender))) {
                Message.COMMAND_NO_PERMISSION.send(sender);
                return;
            }
        }

        if (sub.getArgumentCheck().test(args.size() - this.type.minArgs)) {
            sub.sendDetailedUsage(sender, label);
            return;
        }

        if (this.type == Type.TARGETED) {
            String targetArgument = args.getFirst();

            var targetId = this.parseTarget(targetArgument, plugin, sender);
            if (targetId == null) return;

            var target = this.getTarget(targetId, plugin, sender);
            if (target == null) return;

            try {
                sub.execute(plugin, sender, target, args.subList(this.type.minArgs, args.size()), label);
            } catch (CommandException e) {
                e.handle(sender, label, sub);
            }
        } else {
            try {
                sub.execute(plugin, sender, null, args.subList(this.type.minArgs, args.size()), label);
            } catch (CommandException e) {
                e.handle(sender, label, sub);
            }
        }
    }

    @Override
    public List<String> tabComplete(MoonRisePlugin plugin, Sender sender, ArgumentList args) {
        return switch (this.type) {
            case TARGETED -> TabCompleter.create()
                    .at(0, CompletionSupplier.startsWith(() -> getTargets(plugin).stream()))
                    .at(1, CompletionSupplier.startsWith(() -> this.children.stream()
                            .filter(s -> s.shouldDisplay(sender))
                            .filter(s -> s.isAuthorized(sender))
                            .map(s -> s.getName().toLowerCase(Locale.ROOT))
                    ))
                    .from(2, partial -> this.children.stream()
                            .filter(s -> s.shouldDisplay(sender))
                            .filter(s -> s.isAuthorized(sender))
                            .filter(s -> s.getName().equalsIgnoreCase(args.get(1)))
                            .findFirst()
                            .map(cmd -> cmd.tabComplete(plugin, sender, args.subList(2, args.size())))
                            .orElse(Collections.emptyList())
                    )
                    .complete(args);
            case NOT_TARGETED -> TabCompleter.create()
                    .at(0, CompletionSupplier.startsWith(() -> this.children.stream()
                            .filter(s -> s.shouldDisplay(sender))
                            .filter(s -> s.isAuthorized(sender))
                            .map(s -> s.getName().toLowerCase(Locale.ROOT))
                    ))
                    .from(1, partial -> this.children.stream()
                            .filter(s -> s.shouldDisplay(sender))
                            .filter(s -> s.isAuthorized(sender))
                            .filter(s -> s.getName().equalsIgnoreCase(args.getFirst()))
                            .findFirst()
                            .map(cmd -> cmd.tabComplete(plugin, sender, args.subList(1, args.size())))
                            .orElse(Collections.emptyList())
                    )
                    .complete(args);
        };
    }

    @Override
    public void sendUsage(Sender sender, String label) {
        List<Command<?>> subs = getChildren().stream()
                .filter(s -> s.isAuthorized(sender))
                .collect(ImmutableCollectors.toList());

        if (!subs.isEmpty()) {
            Message.MAIN_COMMAND_USAGE_HEADER.send(sender, getName(), String.format(getUsage(), label));
            for (Command<?> c : subs) {
                c.sendUsage(sender, label);
            }
        } else {
            Message.COMMAND_NO_PERMISSION.send(sender);
        }
    }

    @Override
    public void sendDetailedUsage(Sender sender, String label) {
        sendUsage(sender, label);
    }

    @Override
    public boolean isAuthorized(Sender sender) {
        return this.children.stream().anyMatch(c -> c.isAuthorized(sender));
    }

    protected List<String> getTargets(MoonRisePlugin plugin) {
        throw new UnsupportedOperationException();
    }

    protected I parseTarget(String target, MoonRisePlugin plugin, Sender sender) {
        throw new UnsupportedOperationException();
    }

    protected T getTarget(I target, MoonRisePlugin plugin, Sender sender) {
        throw new UnsupportedOperationException();
    }

    public enum Type {
        // e.g. /m log sub-command....
        NOT_TARGETED(0),

        // e.g. /m user <USER> sub-command....
        TARGETED(1);

        private final int index;
        private final int minArgs;

        Type(int index) {
            this.index = index;
            this.minArgs = index + 1;
        }
    }
}
