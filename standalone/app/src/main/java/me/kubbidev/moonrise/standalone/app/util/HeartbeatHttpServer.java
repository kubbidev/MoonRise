package me.kubbidev.moonrise.standalone.app.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.kubbidev.api.platform.Health;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class HeartbeatHttpServer implements HttpHandler, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(HeartbeatHttpServer.class);

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("heartbeat-http-server-%d")
            .build()
    );

    public static HeartbeatHttpServer createAndStart(int port, Supplier<Health> healthReporter) {
        HeartbeatHttpServer socket = null;

        try {
            socket = new HeartbeatHttpServer(healthReporter, port);
            LOGGER.info("Started healthcheck HTTP server on :{}", port);
        } catch (Exception e) {
            LOGGER.error("Error starting Heartbeat HTTP server", e);
        }

        return socket;
    }

    private final Supplier<Health> healthReporter;
    private final HttpServer server;

    public HeartbeatHttpServer(Supplier<Health> healthReporter, int port) throws IOException {
        this.healthReporter = healthReporter;
        this.server = HttpServer.create(new InetSocketAddress(port), 50);
        this.server.createContext("/health", this);
        this.server.setExecutor(EXECUTOR);
        this.server.start();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Health health = this.healthReporter.get();
        byte[] response = health.toString().getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(health.isHealthy() ? 200 : 503, response.length);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(response);
        }
    }

    @Override
    public void close() {
        this.server.stop(0);
    }
}
