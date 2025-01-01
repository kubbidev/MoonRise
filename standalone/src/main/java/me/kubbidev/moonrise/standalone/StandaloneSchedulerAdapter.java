package me.kubbidev.moonrise.standalone;

import me.kubbidev.moonrise.common.plugin.bootstrap.MoonRiseBootstrap;
import me.kubbidev.moonrise.common.plugin.scheduler.AbstractJavaScheduler;
import me.kubbidev.moonrise.common.plugin.scheduler.SchedulerAdapter;

import java.util.concurrent.Executor;

public class StandaloneSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {

    public StandaloneSchedulerAdapter(MoonRiseBootstrap bootstrap) {
        super(bootstrap);
    }

    @Override
    public Executor sync() {
        return this.async();
    }
}