package com.kingbrezz.tickscope;

import com.kingbrezz.tickscope.command.TickScopeCommand;
import com.kingbrezz.tickscope.monitor.PerformanceMonitor;
import com.kingbrezz.tickscope.monitor.SpikeDetector;
import org.bukkit.plugin.java.JavaPlugin;

public final class TickScope extends JavaPlugin {

    private static TickScope instance;

    private PerformanceMonitor performanceMonitor;
    private SpikeDetector spikeDetector;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        performanceMonitor = new PerformanceMonitor(this);
        performanceMonitor.start();

        spikeDetector = new SpikeDetector(this, performanceMonitor);
        spikeDetector.start();

        TickScopeCommand command = new TickScopeCommand(this);

        if (getCommand("tickscope") != null) {
            getCommand("tickscope").setExecutor(command);
        }

        getLogger().info("=================================");
        getLogger().info(" TickScope 1.0.0");
        getLogger().info(" Performance Monitor & Analyzer");
        getLogger().info(" Author: KingBrezz");
        getLogger().info("=================================");
        getLogger().info("TickScope enabled successfully.");
    }

    @Override
    public void onDisable() {
        if (spikeDetector != null) {
            spikeDetector.stop();
        }

        getLogger().info("TickScope disabled.");

        instance = null;
    }

    public static TickScope getInstance() {
        return instance;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public SpikeDetector getSpikeDetector() {
        return spikeDetector;
    }
}
