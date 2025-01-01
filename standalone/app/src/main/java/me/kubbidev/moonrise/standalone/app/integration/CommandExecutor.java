package me.kubbidev.moonrise.standalone.app.integration;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal command executor interface.
 */
public interface CommandExecutor {

    CompletableFuture<Void> execute(StandaloneSender sender, String command);

    List<String> tabComplete(StandaloneSender sender, String command);

    default CompletableFuture<Void> execute(String command) {
        return execute(StandaloneUser.INSTANCE, command);
    }

    default List<String> tabComplete(String command) {
        return tabComplete(StandaloneUser.INSTANCE, command);
    }
}