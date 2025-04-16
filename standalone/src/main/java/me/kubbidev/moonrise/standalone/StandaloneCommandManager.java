package me.kubbidev.moonrise.standalone;

import me.kubbidev.moonrise.common.sender.command.CommandManager;
import me.kubbidev.moonrise.common.sender.command.util.ArgumentTokenizer;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.standalone.app.integration.CommandExecutor;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneSender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StandaloneCommandManager extends CommandManager implements CommandExecutor {

    private final MStandalonePlugin plugin;

    public StandaloneCommandManager(MStandalonePlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> execute(StandaloneSender sender, String command) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(sender);
        List<String> arguments = ArgumentTokenizer.EXECUTE.tokenizeInput(command);
        return executeCommand(wrapped, "m", arguments);
    }

    @Override
    public List<String> tabComplete(StandaloneSender sender, String command) {
        Sender wrapped = this.plugin.getSenderFactory().wrap(sender);
        List<String> arguments = ArgumentTokenizer.TAB_COMPLETE.tokenizeInput(command);
        return tabCompleteCommand(wrapped, arguments);
    }
}
