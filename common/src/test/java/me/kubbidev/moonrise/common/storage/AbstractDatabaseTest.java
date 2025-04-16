package me.kubbidev.moonrise.common.storage;

import me.kubbidev.moonrise.common.config.MoonRiseConfiguration;
import me.kubbidev.moonrise.common.storage.implementation.StorageImplementation;
import me.kubbidev.moonrise.common.event.EventDispatcher;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;
import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractDatabaseTest {

    protected @Mock MoonRisePlugin        plugin;
    protected @Mock MoonRiseBootstrap     bootstrap;
    protected @Mock MoonRiseConfiguration configuration;
    protected       StorageImplementation database;

    @BeforeEach
    public final void setupMocksAndDatabase() throws Exception {
        lenient().when(this.plugin.getBootstrap()).thenReturn(this.bootstrap);
        lenient().when(this.plugin.getConfiguration()).thenReturn(this.configuration);
        lenient().when(this.plugin.getEventDispatcher()).thenReturn(mock(EventDispatcher.class));
        lenient().when(this.bootstrap.getScheduler()).thenReturn(mock(SchedulerAdapter.class));
        lenient().when(this.bootstrap.getResourceStream(anyString()))
            .then(answer((String path) -> AbstractDatabaseTest.class.getClassLoader().getResourceAsStream(path)));
        lenient().when(this.plugin.getEventDispatcher()).thenReturn(mock(EventDispatcher.class));

        this.database = makeDatabase(this.plugin);
        this.database.init();
    }

    protected abstract StorageImplementation makeDatabase(MoonRisePlugin plugin) throws Exception;

    protected void cleanupResources() {
        // do nothing
    }

    @AfterEach
    public final void shutdownStorage() {
        this.database.shutdown();
        this.cleanupResources();
    }
}
