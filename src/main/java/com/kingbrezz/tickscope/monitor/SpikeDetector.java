package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

public final class SpikeDetector {
    private final TickScope plugin;
    private final PerformanceMonitor monitor;
    private int taskId = -1;

    public SpikeDetector(TickScope plugin, PerformanceMonitor monitor) {
        this.plugin = plugin; this.monitor = monitor;
    }

    public void start() {
        if (taskId != -1) return;
        long ticks = Math.max(1L, plugin.getConfig().getLong("monitor.interval-ms", 1000L) / 50L);
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::check, ticks, ticks).getTaskId();
    }

    private void check() {
        if (!plugin.getConfig().getBoolean("monitor.enabled", true)) return;
        MetricsSnapshot s = monitor.collect();
        double threshold = plugin.getConfig().getDouble("thresholds.spike-mspt", 50.0);
        if (s.mspt() < threshold) return;
        String severity = s.mspt() >= 100 ? "CRITICAL" : s.mspt() >= 75 ? "HIGH" : "WARNING";
        plugin.getLogger().warning(String.format("Spike: %.1fms MSPT | %.2f TPS | %s", s.mspt(), s.tps(), severity));
    }

    public void stop() {
        if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; }
    }
}
