package me.kubbidev.moonrise.common.util;

import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Base class for an interface which can perform operations asynchronously and return {@link CompletableFuture}s
 */
public abstract class AsyncInterface {

    private final MoonRisePlugin plugin;

    protected AsyncInterface(MoonRisePlugin plugin) {
        this.plugin = plugin;
    }

    protected <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }

    protected CompletableFuture<Void> future(Throwing.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.plugin.getBootstrap().getScheduler().async());
    }
}