package me.kubbidev.moonrise.standalone.app;

import me.kubbidev.moonrise.api.MoonRise;
import me.kubbidev.moonrise.standalone.app.integration.CommandExecutor;
import me.kubbidev.moonrise.standalone.app.integration.ShutdownCallback;
import me.kubbidev.moonrise.standalone.app.util.DockerCommandSocket;
import me.kubbidev.moonrise.standalone.app.util.HeartbeatHttpServer;
import me.kubbidev.moonrise.standalone.app.util.TerminalInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The MoonRise standalone application.
 */
public class MoonRiseApplication implements AutoCloseable {

    /** A logger instance */
    public static final Logger LOGGER = LogManager.getLogger(MoonRiseApplication.class);

    /** A callback to shutdown the application via the loader bootstrap. */
    private final ShutdownCallback shutdownCallback;

    /** The instance of the MoonRise API available within the app */
    private MoonRise moonRiseApi;

    /** A command executor interface to run MoonRise commands */
    private CommandExecutor commandExecutor;

    /** If the application is running */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** The docker command socket */
    private DockerCommandSocket dockerCommandSocket;

    /** The heartbeat http server */
    private HeartbeatHttpServer heartbeatHttpServer;

    public MoonRiseApplication(ShutdownCallback shutdownCallback) {
        this.shutdownCallback = shutdownCallback;
    }

    /**
     * Start the app
     */
    public void start(String[] args) {
        TerminalInterface terminal = new TerminalInterface(this, this.commandExecutor);

        List<String> arguments = Arrays.asList(args);
        if (arguments.contains("--docker")) {
            this.dockerCommandSocket = DockerCommandSocket.createAndStart("/opt/moonrise/moonrise.sock", terminal);
            this.heartbeatHttpServer = HeartbeatHttpServer.createAndStart(3001, () -> this.moonRiseApi.runHealthCheck());
        }

        terminal.start(); // blocking
    }

    public void requestShutdown() {
        this.shutdownCallback.shutdown();
    }

    @Override
    public void close() {
        this.running.set(false);

        if (this.dockerCommandSocket != null) {
            try {
                this.dockerCommandSocket.close();
            } catch (Exception e) {
                LOGGER.warn(e);
            }
        }

        if (this.heartbeatHttpServer != null) {
            try {
                this.heartbeatHttpServer.close();
            } catch (Exception e) {
                LOGGER.warn(e);
            }
        }
    }

    public AtomicBoolean runningState() {
        return this.running;
    }

    // called before start()
    public void setApi(MoonRise moonRiseApi) {
        this.moonRiseApi = moonRiseApi;
    }

    // called before start()
    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public MoonRise getApi() {
        return this.moonRiseApi;
    }

    public CommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public String getVersion() {
        return "@version@";
    }
}
