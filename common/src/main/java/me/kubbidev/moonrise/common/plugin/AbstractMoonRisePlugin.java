package me.kubbidev.moonrise.common.plugin;

import me.kubbidev.moonrise.api.MoonRise;
import me.kubbidev.moonrise.api.platform.Health;
import me.kubbidev.moonrise.common.api.ApiRegistrationUtil;
import me.kubbidev.moonrise.common.api.MoonRiseApiProvider;
import me.kubbidev.moonrise.common.config.ConfigKeys;
import me.kubbidev.moonrise.common.config.MoonRiseConfiguration;
import me.kubbidev.moonrise.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.moonrise.common.config.generic.adapter.EnvironmentVariableConfigAdapter;
import me.kubbidev.moonrise.common.config.generic.adapter.MultiConfigurationAdapter;
import me.kubbidev.moonrise.common.config.generic.adapter.SystemPropertyConfigAdapter;
import me.kubbidev.moonrise.common.gateway.GatewayClient;
import me.kubbidev.moonrise.common.storage.Storage;
import me.kubbidev.moonrise.common.storage.StorageMetadata;
import me.kubbidev.moonrise.common.storage.StorageType;
import me.kubbidev.moonrise.common.dependencies.Dependency;
import me.kubbidev.moonrise.common.dependencies.DependencyManager;
import me.kubbidev.moonrise.common.dependencies.DependencyManagerImpl;
import me.kubbidev.moonrise.common.event.AbstractEventBus;
import me.kubbidev.moonrise.common.event.EventDispatcher;
import me.kubbidev.moonrise.common.extension.SimpleExtensionManager;
import me.kubbidev.moonrise.common.http.BytebinClient;
import me.kubbidev.moonrise.common.locale.Message;
import me.kubbidev.moonrise.common.locale.TranslationManager;
import me.kubbidev.moonrise.common.locale.TranslationRepository;
import me.kubbidev.moonrise.common.plugin.logging.PluginLogger;
import me.kubbidev.moonrise.common.plugin.util.HealthCheckResult;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMoonRisePlugin implements MoonRisePlugin {

    // init during load
    private DependencyManager dependencyManager;
    private TranslationManager translationManager;

    // init during enable
    private MoonRiseConfiguration configuration;
    private OkHttpClient httpClient;
    private BytebinClient bytebin;
    private TranslationRepository translationRepository;
    private Storage storage;
    private MoonRiseApiProvider apiProvider;
    private EventDispatcher eventDispatcher;
    private SimpleExtensionManager extensionManager;
    private GatewayClient gateway;

    private boolean running = false;

    /**
     * Performs the initial actions to load the plugin
     */
    public final void load() {
        getLogger().info("Loading dependencies, please wait...");

        // load dependencies
        this.dependencyManager = createDependencyManager();
        this.dependencyManager.loadDependencies(getGlobalDependencies());

        // load translations
        this.translationManager = new TranslationManager(this);
        this.translationManager.reload();
    }

    public final void enable() {
        // load the sender factory instance
        this.setupSenderFactory();

        // send the startup banner
        Message.STARTUP_BANNER.send(getConsoleSender(), getBootstrap());

        // load configuration
        getLogger().info("Loading configuration...");
        ConfigurationAdapter configFileAdapter = provideConfigurationAdapter();
        this.configuration = new MoonRiseConfiguration(this, new MultiConfigurationAdapter(this,
                new SystemPropertyConfigAdapter(this),
                new EnvironmentVariableConfigAdapter(this),
                configFileAdapter
        ));

        // setup a bytebin instance
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(15, TimeUnit.SECONDS)
                .build();

        this.bytebin = new BytebinClient(
                this.httpClient,
                getConfiguration().get(ConfigKeys.BYTEBIN_URL),
                "moonrise"
        );

        // init translation repo and update bundle files
        this.translationRepository = new TranslationRepository(this);
        this.translationRepository.scheduleRefresh();

        // now the configuration is loaded, we can create a storage factory and load initial dependencies
        this.dependencyManager.loadStorageDependencies(StorageType.getRequiredType(this));

        // register listeners
        this.registerPlatformListeners();

        // initialise storage
        this.storage = StorageType.getInstance(this);

        // register commands
        this.registerCommands();

        // setup guild/user/member manager
        this.setupManagers();

        // establish the connection
        this.gateway = new GatewayClient(this, this.storage);
        this.gateway.connect(getConfiguration().get(ConfigKeys.AUTHENTICATION_TOKEN));

        // setup platform hooks
        this.setupPlatformHooks();

        // register with the MoonRise API
        this.apiProvider = new MoonRiseApiProvider(this);
        this.apiProvider.ensureApiWasLoadedByPlugin();
        this.eventDispatcher = new EventDispatcher(provideEventBus(this.apiProvider));
        ApiRegistrationUtil.registerProvider(this.apiProvider);
        this.registerApiOnPlatform(this.apiProvider);

        // setup extension manager
        this.extensionManager = new SimpleExtensionManager(this);
        this.extensionManager.loadExtensions(getBootstrap().getConfigDirectory().resolve("extensions"));

        // perform any platform-specific final setup tasks
        this.performFinalSetup();

        // mark as running
        this.running = true;

        Duration timeTaken = getBootstrap().getStartupDuration();
        getLogger().info("Successfully enabled. (took " + timeTaken.toMillis() + "ms)");
    }

    public final void disable() {
        getLogger().info("Starting shutdown process...");

        // cancel delayed/repeating tasks
        getBootstrap().getScheduler().shutdownScheduler();

        // close connection
        this.gateway.close();

        // unload extensions
        this.extensionManager.close();

        // mark as not running
        this.running = false;

        // remove any hooks into the platform
        this.removePlatformHooks();

        // close storage
        getLogger().info("Closing storage...");
        this.storage.shutdown();

        // unregister api
        ApiRegistrationUtil.unregisterProvider();

        // shutdown async executor pool
        getBootstrap().getScheduler().shutdownExecutor();

        // shutdown okhttp
        ExecutorService executorService = this.httpClient.dispatcher().executorService();
        executorService.shutdown();
        this.httpClient.connectionPool().evictAll();

        // close isolated loaders for non-relocated dependencies
        getDependencyManager().close();

        // close classpath appender
        getBootstrap().getClassPathAppender().close();

        getLogger().info("Goodbye!");
    }

    // hooks called during load

    protected DependencyManager createDependencyManager() {
        return new DependencyManagerImpl(this);
    }

    protected Set<Dependency> getGlobalDependencies() {
        return EnumSet.of(
                Dependency.ADVENTURE,
                Dependency.CAFFEINE,
                Dependency.OKIO,
                Dependency.OKHTTP,
                Dependency.EVENT,
                Dependency.JACKSON_ANNOTATIONS,
                Dependency.JACKSON_CORE,
                Dependency.JACKSON_DATABIND,
                Dependency.TROVE4J,
                Dependency.NEOVISIONARIES,
                Dependency.COLLECTIONS4,
                Dependency.JDA
        );
    }

    // hooks called during enable

    protected abstract void setupSenderFactory();

    protected abstract ConfigurationAdapter provideConfigurationAdapter();

    protected abstract void registerPlatformListeners();

    protected abstract void registerCommands();

    protected abstract void setupManagers();

    protected abstract void setupPlatformHooks();

    protected abstract AbstractEventBus<?> provideEventBus(MoonRiseApiProvider apiProvider);

    protected abstract void registerApiOnPlatform(MoonRise api);

    protected abstract void performFinalSetup();

    // hooks called during disable

    protected void removePlatformHooks() {}

    protected Path resolveConfig(String fileName) {
        Path configFile = getBootstrap().getConfigDirectory().resolve(fileName);

        // if the config doesn't exist, create it based on the template in the resources dir
        if (!Files.exists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                // ignore
            }

            try (InputStream is = getBootstrap().getResourceStream(fileName)) {
                Files.copy(is, configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return configFile;
    }

    @Override
    public PluginLogger getLogger() {
        return getBootstrap().getPluginLogger();
    }

    @Override
    public Health runHealthCheck() {
        if (!this.running) {
            return HealthCheckResult.unhealthy(Collections.emptyMap());
        }

        StorageMetadata meta = this.storage.getMeta();
        if (meta.connected() != null && !meta.connected()) {
            return HealthCheckResult.unhealthy(Collections.singletonMap("reason", "storage disconnected"));
        }

        Map<String, Object> map = new LinkedHashMap<>();
        if (meta.connected() != null) {
            map.put("storageConnected", meta.connected());
        }
        if (meta.ping() != null) {
            map.put("storagePing", meta.ping());
        }
        if (meta.sizeBytes() != null) {
            map.put("storageSizeBytes", meta.sizeBytes());
        }

        return HealthCheckResult.healthy(map);
    }

    @Override
    public GatewayClient getGatewayClient() {
        return this.gateway;
    }

    @Override
    public DependencyManager getDependencyManager() {
        return this.dependencyManager;
    }

    @Override
    public TranslationManager getTranslationManager() {
        return this.translationManager;
    }

    @Override
    public MoonRiseConfiguration getConfiguration() {
        return this.configuration;
    }

    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    @Override
    public BytebinClient getBytebin() {
        return this.bytebin;
    }

    @Override
    public TranslationRepository getTranslationRepository() {
        return this.translationRepository;
    }

    @Override
    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public MoonRiseApiProvider getApiProvider() {
        return this.apiProvider;
    }

    @Override
    public SimpleExtensionManager getExtensionManager() {
        return this.extensionManager;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public static String getPluginName() {
        LocalDate date = LocalDate.now();
        if (date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1) {
            return "SunSet";
        }
        return "MoonRise";
    }
}
