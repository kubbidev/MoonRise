package me.kubbidev.moonrise.common.command;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.kubbidev.moonrise.common.commands.AboutCommand;
import me.kubbidev.moonrise.common.commands.BiographyCommand;
import me.kubbidev.moonrise.common.commands.PingCommand;
import me.kubbidev.moonrise.common.commands.ProfileCommand;
import me.kubbidev.moonrise.common.commands.LeaderboardChannelCommand;
import me.kubbidev.moonrise.common.commands.LeaderboardEnabledCommand;
import me.kubbidev.moonrise.common.commands.LeaderboardRefreshCommand;
import me.kubbidev.moonrise.common.message.source.MessageChannelSource;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerTask;
import me.kubbidev.moonrise.common.message.source.Source;
import me.kubbidev.moonrise.common.util.CompletableFutures;
import me.kubbidev.moonrise.common.util.ImmutableCollectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InteractionManager extends ListenerAdapter {

    private final MoonRisePlugin plugin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("moonrise-interaction-executor")
            .build()
    );
    private final AtomicBoolean executingCommand = new AtomicBoolean(false);
    private final Map<String, Interaction> interactions;

    public InteractionManager(MoonRisePlugin plugin) {
        this.plugin = plugin;
        this.interactions = ImmutableList.<Interaction>builder()
                .add(new PingCommand())
                .add(new AboutCommand())
                .add(new BiographyCommand())
                .add(new ProfileCommand())
                .add(new LeaderboardChannelCommand())
                .add(new LeaderboardEnabledCommand())
                .add(new LeaderboardRefreshCommand())
                .build()
                .stream()
                .collect(ImmutableCollectors.toMap(c -> c.getMetadata().getName().toLowerCase(Locale.ROOT), Function.identity()));
    }

    public MoonRisePlugin getPlugin() {
        return this.plugin;
    }

    @VisibleForTesting
    public Map<String, Interaction> getInteractions() {
        return this.interactions;
    }

    /**
     * Registers the {@link Interaction}s with the specified JDA (Java Discord API) shard.
     * <p>
     * This method updates and queues the commands associated with the interactions currently
     * available in the manager.
     *
     * @param shard The JDA instance representing a shard.
     */
    public void registerInteraction(@NotNull JDA shard) {
        var commands = shard.updateCommands();

        for (Interaction i : this.interactions.values()) {
            commands = commands.addCommands(i.getMetadata());
        }

        commands.queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        var wrapped = MessageChannelSource.wrap(e.getChannel());
        this.executeCommand(wrapped, e.getName(), new InteractionContext(e));
    }

    public CompletableFuture<Void> executeCommand(Source source, String label, InteractionContext context) {
        SchedulerAdapter scheduler = this.plugin.getBootstrap().getScheduler();

        // if the executingCommand flag is set, there is another command executing at the moment
        if (this.executingCommand.get()) {
            context.setDeferred(true);
            context.sendMessage(Message.ALREADY_EXECUTING_COMMAND.build());
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
                this.execute(source, label, context);
            } catch (Throwable e) {
                this.handleStackTrace(context, e);
//                Throwable cause = e;
//                do {
//                    this.handleStackTrace(context, cause);
//                } while ((cause = cause.getCause()) != null);

                // catch any exception
                this.plugin.getLogger().severe("Exception whilst executing command: " + label, e);
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
                this.handleCommandTimeout(executorThread, label);
            }
        }, 10, TimeUnit.SECONDS));

        return future;
    }

    private void handleStackTrace(InteractionContext context, Throwable e) {
        var output = new StringWriter();
        var writer = new PrintWriter(output);
        e.printStackTrace(writer);

        List<ComponentLike> trace = new ArrayList<>();
        trace.add(Message.COMMAND_EXECUTION_EXCEPTION_HEADER.build());
        trace.add(Component.text("```"));
        trace.add(Component.text(output.toString()));

        trace.add(Component.text("```"));
        Component stackedTrace = Component.join(JoinConfiguration.newlines(), trace);

        context.setDeferred(false);
        context.sendMessage(stackedTrace);
    }

    private void handleCommandTimeout(AtomicReference<Thread> thread, String label) {
        Thread executorThread = thread.get();
        if (executorThread == null) {
            this.plugin.getLogger().warn("Interaction execution " + label + " has not completed - is another interaction execution blocking it?");
        } else {
            String stackTrace = Arrays.stream(executorThread.getStackTrace())
                    .map(s -> "  " + s)
                    .collect(Collectors.joining("\n"));
            this.plugin.getLogger().warn("Interaction execution " + label + " has not completed. Trace: \n" + stackTrace);
        }
    }

    private void execute(Source source, String label, InteractionContext context) {
        Interaction main = this.interactions.get(label.toLowerCase(Locale.ROOT));

        // Try to execute the interaction.
        if (main != null) {
            main.execute(this.plugin, source, context);
        } else {
            context.setDeferred(true);
            context.sendMessage(Message.COMMAND_NOT_RECOGNISED.build());
        }
    }
}