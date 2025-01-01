package me.kubbidev.moonrise.standalone.app.util;

import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import me.kubbidev.moonrise.standalone.app.integration.CommandExecutor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;

import java.util.List;

/**
 * The terminal/console-style interface presented to the user.
 */
public class TerminalInterface extends SimpleTerminalConsole {
    private final MoonRiseApplication application;
    private final CommandExecutor commandExecutor;

    public TerminalInterface(MoonRiseApplication application, CommandExecutor commandExecutor) {
        this.application = application;
        this.commandExecutor = commandExecutor;
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder
                .appName("MoonRise")
                .completer(this::completeCommand)
        );
    }

    @Override
    protected boolean isRunning() {
        return this.application.runningState().get();
    }

    @Override
    protected void shutdown() {
        this.application.requestShutdown();
    }

    @Override
    protected void runCommand(String command) {
        command = stripSlashM(command);

        if (command.equalsIgnoreCase("stop") || command.equalsIgnoreCase("end")) {
            this.application.requestShutdown();
            return;
        }

        this.commandExecutor.execute(command);
    }

    private void completeCommand(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String cmdLine = stripSlashM(line.line());

        for (String suggestion : this.commandExecutor.tabComplete(cmdLine)) {
            candidates.add(new Candidate(suggestion));
        }
    }

    private static String stripSlashM(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (command.startsWith("m ")) {
            command = command.substring(2);
        }
        return command;
    }
}