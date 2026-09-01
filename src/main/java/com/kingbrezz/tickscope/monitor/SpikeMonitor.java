package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

public final class SpikeMonitor {
    private final TickScope plugin;
    private final PerformanceMonitor monitor;
    private final SpikeHistory history = new SpikeHistory();
    private int taskId = -1;

    public SpikeMonitor(TickScope plugin, PerformanceMonitor monitor) {
        this.plugin = plugin;
        this.monitor = monitor;
    }

    public void start() {
        if (taskId != -1) return;
        long ticks = Math.max(1L, plugin.getConfig().getLong("spikes.interval-ms", 1000L) / 50L);
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::check, ticks, ticks).getTaskId();
    }

    private void check() {
        if (!plugin.getConfig().getBoolean("monitor.enabled", true)) return;
        MetricsSnapshot s = monitor.collect();
        double warn = plugin.getConfig().getDouble("thresholds.warning-mspt", 40.0);
        double critical = plugin.getConfig().getDouble("thresholds.critical-mspt", 50.0);
        if (s.mspt() < warn && s.tps() >= 18.0) return;

        String severity = s.mspt() >= critical || s.tps() < 15.0 ? "CRITICAL" : "WARNING";
        history.add(new SpikeEvent(s.timestamp(), s.tps(), s.mspt(), s.players(),
                s.loadedChunks(), s.entities(), s.tileEntities(), severity));

        int max = Math.max(10, plugin.getConfig().getInt("history.max-spikes", 200));
        history.trimTo(max);
    }

    public SpikeHistory getHistory() { return history; }
    public void clear() { history.clear(); }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
