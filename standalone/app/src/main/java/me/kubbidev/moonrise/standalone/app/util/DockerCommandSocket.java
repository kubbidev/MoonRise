package me.kubbidev.moonrise.standalone.app.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DockerCommandSocket implements Runnable, AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger(DockerCommandSocket.class);

    public static DockerCommandSocket createAndStart(String socketPath, TerminalInterface terminal) {
        DockerCommandSocket socket = null;

        try {
            Path path = Path.of(socketPath);
            Files.deleteIfExists(path);

            ServerSocketChannel channel = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
            channel.bind(UnixDomainSocketAddress.of(path));

            socket = new DockerCommandSocket(channel, terminal::runCommand);

            Thread thread = new Thread(socket, "docker-command-socket");
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            LOGGER.error("Error starting docker command socket", e);
        }

        return socket;
    }

    private final ServerSocketChannel channel;
    private final Consumer<String>    callback;

    public DockerCommandSocket(ServerSocketChannel channel, Consumer<String> callback) {
        this.channel = channel;
        this.callback = callback;
    }

    @Override
    public void run() {
        while (this.channel.isOpen()) {
            try (SocketChannel socket = this.channel.accept()) {
                try (BufferedReader reader = new BufferedReader(Channels.newReader(socket, StandardCharsets.UTF_8))) {
                    String cmd;
                    while ((cmd = reader.readLine()) != null) {
                        LOGGER.info("Executing command from Docker: {}", cmd);
                        this.callback.accept(cmd);
                    }
                }
            } catch (ClosedChannelException e) {
                // ignore
            } catch (IOException e) {
                LOGGER.error("Error processing input from the Docker socket", e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        this.channel.close();
    }
}
