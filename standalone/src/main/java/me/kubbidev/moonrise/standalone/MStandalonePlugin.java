package me.kubbidev.moonrise.standalone;

import me.kubbidev.moonrise.api.MoonRise;
import me.kubbidev.moonrise.common.api.MoonRiseApiProvider;
import me.kubbidev.moonrise.common.event.AbstractEventBus;
import me.kubbidev.moonrise.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.moonrise.common.dependencies.Dependency;
import me.kubbidev.moonrise.common.plugin.AbstractMoonRisePlugin;
import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.standalone.app.MoonRiseApplication;
import me.kubbidev.moonrise.standalone.app.integration.StandaloneUser;
import me.kubbidev.moonrise.standalone.stub.StandaloneEventBus;

import java.util.Set;
import java.util.stream.Stream;

/**
 * MoonRise implementation for the standalone app.
 */
public class MStandalonePlugin extends AbstractMoonRisePlugin {

    private final MStandaloneBootstrap bootstrap;

    private StandaloneSenderFactory  senderFactory;
    private StandaloneCommandManager commandManager;

    public MStandalonePlugin(MStandaloneBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public MStandaloneBootstrap getBootstrap() {
        return this.bootstrap;
    }

    public MoonRiseApplication getLoader() {
        return this.bootstrap.getLoader();
    }

    @Override
    protected void setupSenderFactory() {
        this.senderFactory = new StandaloneSenderFactory(this);
    }

    @Override
    protected Set<Dependency> getGlobalDependencies() {
        Set<Dependency> dependencies = super.getGlobalDependencies();
        dependencies.remove(Dependency.ADVENTURE);
        dependencies.add(Dependency.CONFIGURATE_CORE);
        dependencies.add(Dependency.CONFIGURATE_YAML);
        dependencies.add(Dependency.SNAKEYAML);
        return dependencies;
    }

    @Override
    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new StandaloneConfigAdapter(this, resolveConfig("config.yml"));
    }

    @Override
    protected void registerPlatformListeners() {

    }

    @Override
    protected void registerCommands() {
        this.commandManager = new StandaloneCommandManager(this);
        this.bootstrap.getLoader().setCommandExecutor(this.commandManager);
    }

    @Override
    protected void setupManagers() {

    }

    @Override
    protected void setupPlatformHooks() {

    }

    @Override
    protected AbstractEventBus<?> provideEventBus(MoonRiseApiProvider apiProvider) {
        return new StandaloneEventBus(this, apiProvider);
    }

    @Override
    protected void registerApiOnPlatform(MoonRise api) {
        this.bootstrap.getLoader().setApi(api);
    }

    @Override
    protected void performFinalSetup() {

    }

    @Override
    public Stream<Sender> getOnlineSenders() {
        return Stream.of(getConsoleSender());
    }

    @Override
    public Sender getConsoleSender() {
        return getSenderFactory().wrap(StandaloneUser.INSTANCE);
    }

    public StandaloneSenderFactory getSenderFactory() {
        return this.senderFactory;
    }

    @Override
    public StandaloneCommandManager getCommandManager() {
        return this.commandManager;
    }
}
