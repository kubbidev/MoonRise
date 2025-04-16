package me.kubbidev.moonrise.standalone.stub;

import me.kubbidev.moonrise.common.api.MoonRiseApiProvider;
import me.kubbidev.moonrise.common.event.AbstractEventBus;
import me.kubbidev.moonrise.common.plugin.MoonRisePlugin;

public class StandaloneEventBus extends AbstractEventBus<Object> {

    public StandaloneEventBus(MoonRisePlugin plugin, MoonRiseApiProvider apiProvider) {
        super(plugin, apiProvider);
    }

    @Override
    protected Object checkPlugin(Object plugin) throws IllegalArgumentException {
        return plugin;
    }
}
