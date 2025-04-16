package me.kubbidev.moonrise.common.sender.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.sender.command.abstraction.Command;
import me.kubbidev.moonrise.common.sender.command.abstraction.ParentCommand;
import me.kubbidev.moonrise.common.sender.command.access.BuiltinPermission;
import me.kubbidev.moonrise.common.sender.command.spec.BuildinDefinition;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.CompletionSupplier;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.TabCompleter;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.util.Predicates;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("Help", BuildinDefinition.HELP, BuiltinPermission.HELP, Predicates.alwaysFalse());
    }

    private static Map<String, Command> getChildren(MoonRisePlugin plugin) {
        return plugin.getCommandManager().getMainCommands();
    }

    @Override
    public void execute(MoonRisePlugin plugin, Sender sender, ArgumentList args, String label) {
        if (args.isEmpty()) {
            plugin.getCommandManager().sendCommandUsage(sender, label);
        } else {
            List<Command> commandTree = new ArrayList<>();
            Collection<Command> availableCmd = getChildren(plugin).values();
            for (String arg : args) {
                if (availableCmd.isEmpty()) {
                    Message.COMMAND_NOT_RECOGNISED.send(sender);
                    return;
                }

                Optional<Command> command = getMatchingCommand(availableCmd, arg, sender);

                if (command.isEmpty()) {
                    Message.COMMAND_NOT_RECOGNISED.send(sender);
                    return;
                }

                commandTree.add(command.get());
                if (!(command.get() instanceof ParentCommand)) {
                    availableCmd = Collections.emptyList();
                } else {
                    availableCmd = ((ParentCommand) command.get()).getChildren();
                }
            }

            Command command = commandTree.getLast();
            StringBuilder path = new StringBuilder();

            long limit = commandTree.size() - 1;
            for (Command child : commandTree) {
                if (limit-- == 0) {
                    break;
                }
                path.append(child.getName().toLowerCase(Locale.ROOT));
                path.append(" ");
            }

            if (command != null) {
                path.append(command.getName().toLowerCase(Locale.ROOT));
                command.sendDetailedUsage(sender, path.toString());
            }
        }
    }

    protected List<Command> getMatchingCommands(Collection<Command> commands, String partial) {
        return commands.stream().filter(c -> Predicates.startsWithIgnoreCase(partial).test(c.getName())).collect(
            Collectors.toList());
    }

    protected Optional<Command> getMatchingCommand(Collection<Command> commands, String matching, Sender sender) {
        return commands.stream().filter(c -> c.getName().equalsIgnoreCase(matching))
            .max(Comparator.comparing(c -> c.hasPermission(sender)));
    }

    @Override
    public List<String> tabComplete(MoonRisePlugin plugin, Sender sender, ArgumentList args) {
        return TabCompleter.create().from(0, CompletionSupplier.startsWith(() -> tabComplete(plugin, args)))
            .complete(args);
    }

    protected Stream<String> tabComplete(MoonRisePlugin plugin, ArgumentList args) {
        Collection<Command> availableCmd = getChildren(plugin).values();
        for (String arg : args) {
            List<Command> commands = getMatchingCommands(availableCmd, arg);
            if (commands.isEmpty()) {
                return Stream.empty();
            }

            Optional<Command> cmd = commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(arg))
                .findFirst();

            if (cmd.isEmpty()) {
                availableCmd = commands;
            } else {
                if (!(cmd.get() instanceof ParentCommand)) {
                    return Stream.empty();
                } else {
                    availableCmd = ((ParentCommand) cmd.get()).getChildren();
                }
            }
        }

        // Actually completing this arg
        return availableCmd.stream().map(c -> c.getName().toLowerCase(Locale.ROOT)).distinct();
    }
}