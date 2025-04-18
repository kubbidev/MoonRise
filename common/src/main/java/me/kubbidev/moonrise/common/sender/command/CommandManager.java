package me.kubbidev.moonrise.common.sender.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.moonrise.common.sender.command.abstraction.Command;
import me.kubbidev.moonrise.common.sender.command.abstraction.CommandException;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.CompletionSupplier;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.TabCompleter;
import me.kubbidev.moonrise.common.sender.command.tabcomplete.TabCompletions;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentList;
import me.kubbidev.moonrise.common.sender.commands.HelpCommand;
import me.kubbidev.moonrise.common.sender.commands.InfoCommand;
import me.kubbidev.moonrise.common.sender.commands.ReloadConfigCommand;
import me.kubbidev.moonrise.common.sender.commands.TranslationsCommand;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerTask;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import me.kubbidev.moonrise.common.util.ExpiringSet;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Root command manager for the '/moonrise' command.
 */
@SuppressWarnings("InstantiationOfUtilityClass")
public class CommandManager {

    private static final int MAXIMUM_COMMAND_LIST = 120;

    private final MoonRisePlugin       plugin;
    private final ExecutorService      executor         = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("moonrise-command-executor")
        .build()
    );
    private final AtomicBoolean        executingCommand = new AtomicBoolean(false);
    private final TabCompletions       tabCompletions;
    private final Map<String, Command> mainCommands;
    private final Set<UUID>            playerRateLimit  = ExpiringSet.newExpiringSet(500, TimeUnit.MILLISECONDS);

    public CommandManager(MoonRisePlugin plugin) {
        this.plugin = plugin;
        this.tabCompletions = new TabCompletions(plugin);
        this.mainCommands = ImmutableList.<Command>builder()
            .addAll(plugin.getExtraCommands())
            .add(new HelpCommand())
            .add(new InfoCommand())
            .add(new ReloadConfigCommand())
            .add(new TranslationsCommand())
            .build()
            .stream()
            .collect(ImmutableCollectors.toMap(c -> c.getName().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    public TabCompletions getTabCompletions() {
        return this.tabCompletions;
    }

    @VisibleForTesting
    public Map<String, Command> getMainCommands() {
        return this.mainCommands;
    }

    public CompletableFuture<Void> executeCommand(Sender sender, String label, List<String> args) {
        UUID uniqueId = sender.getUniqueId();
        if (this.plugin.getConfiguration().get(ConfigKeys.COMMANDS_RATE_LIMIT) && !sender.isConsole()
            && !this.playerRateLimit.add(uniqueId)) {
            this.plugin.getLogger()
                .warn("Player '" + uniqueId + "' is spamming MoonRise commands. Ignoring further inputs.");
            return CompletableFutures.NULL;
        }

        SchedulerAdapter scheduler = this.plugin.getBootstrap().getScheduler();
        List<String> argsCopy = new ArrayList<>(args);

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            Message.ALREADY_EXECUTING_COMMAND.send(sender);
            return CompletableFutures.NULL;
        }

        // a reference to the thread being used to execute the command
        AtomicReference<Thread> executorThread = new AtomicReference<>();
        // a reference to the timeout task scheduled to catch if this command takes too long to execute
        AtomicReference<SchedulerTask> timeoutTask = new AtomicReference<>();

        // schedule the actual execution of the command using the command executor service
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            // set flags
            executorThread.set(Thread.currentThread());
            this.executingCommand.set(true);

            // actually try to execute the command
            try {
                execute(sender, label, argsCopy);
            } catch (Throwable e) {
                // catch any exception
                this.plugin.getLogger().severe("Exception whilst executing command: " + args, e);
            } finally {
                // unset flags
                this.executingCommand.set(false);
                executorThread.set(null);

                // cancel the timeout task
                SchedulerTask timeout;
                if ((timeout = timeoutTask.get()) != null) {
                    timeout.cancel();
                }
            }
        }, this.executor);

        // schedule another task to catch if the command doesn't complete after 10 seconds
        timeoutTask.set(scheduler.asyncLater(() -> {
            if (!future.isDone()) {
                handleCommandTimeout(executorThread, argsCopy);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, List<String> args) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            this.plugin.getLogger()
                .warn("Command execution " + args + " has not completed - is another command execution blocking it?");
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                .map(s -> "  " + s)
                .collect(Collectors.joining("\n"));
            this.plugin.getLogger().warn("Command execution " + args + " has not completed. Trace: \n" + stackTrace);
        }
    }

    private void execute(Sender sender, String label, List<String> arguments) {
        if (isEmptyCommandCall(sender, label, arguments)) {
            return; // Handle no arguments
        }

        // Look for the main command.
        Command main = this.mainCommands.get(arguments.getFirst().toLowerCase(Locale.ROOT));

        // Main command not found
        if (main == null) {
            Message.PLUGIN_INFO.send(sender, this.plugin.getBootstrap());
            return;
        }

        // Check the Sender has permission to use the main command.
        if (!main.hasPermission(sender)) {
            Message.PLUGIN_INFO.send(sender, this.plugin.getBootstrap());
            return;
        }

        arguments.removeFirst(); // remove the main command arg.

        // Check the correct number of args were given for the main command
        if (main.getArgumentCheck().test(arguments.size())) {
            main.sendDetailedUsage(sender, label);
            return;
        }

        // Try to execute the command.
        try {
            main.execute(this.plugin, sender, new ArgumentList(arguments), label);
        } catch (CommandException e) {
            e.handle(sender, label, main);
        }
    }

    public boolean hasPermissionForAny(Sender sender) {
        return this.mainCommands.values().stream().anyMatch(c -> c.shouldDisplay(sender) && c.hasPermission(sender));
    }

    private boolean isEmptyCommandCall(Sender sender, String label, List<String> arguments) {
        var hasArguments = !arguments.isEmpty();
        if (hasArguments && (arguments.size() != 1 || !arguments.getFirst().trim().isEmpty())) {
            return false;
        }

        Message.PLUGIN_INFO.send(sender, this.plugin.getBootstrap());
        if (hasPermissionForAny(sender)) {
            Message.VIEW_AVAILABLE_COMMANDS_PROMPT.send(sender, label);
            return true;
        }

        Message.NO_PERMISSION_FOR_SUBCOMMANDS.send(sender);
        return true;
    }

    public List<String> tabCompleteCommand(Sender sender, List<String> arguments) {
        List<Command> mains = this.mainCommands.values().stream()
            .filter(m -> m.shouldDisplay(sender))
            .filter(m -> m.hasPermission(sender))
            .collect(ImmutableCollectors.toList());

        return TabCompleter.create()
            .at(0, CompletionSupplier.startsWith(() -> mains.stream().map(c -> c.getName().toLowerCase(Locale.ROOT))))
            .from(1, partial -> mains.stream()
                .filter(c -> c.getName().equalsIgnoreCase(arguments.getFirst()))
                .findFirst()
                .map(c -> c.tabComplete(this.plugin, sender, new ArgumentList(arguments.subList(1, arguments.size()))))
                .orElse(Collections.emptyList())
            )
            .complete(arguments);
    }

    public void sendCommandUsage(Sender sender, String label) {
        Message.MAIN_COMMAND_USAGE_HEADER.send(sender, "");

        int i = 0;
        int j = this.mainCommands.size();
        for (Command c : this.mainCommands.values()) {
            if (i == MAXIMUM_COMMAND_LIST) {
                int remaining = j - i;
                Message.COMMAND_LIST_TOO_MUCH_REMAINING.send(sender, remaining);
                break;
            }

            i++;
            boolean shouldDisplay = c.shouldDisplay(sender);
            boolean hasPermission = c.hasPermission(sender);
            if (shouldDisplay && hasPermission) {
                c.sendUsage(sender, String.format("%s %s", label, c.getName().toLowerCase(Locale.ROOT)));
            }
        }
    }
}