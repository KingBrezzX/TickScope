package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class PerformanceMonitor {
    private final TickScope plugin;
    private final AtomicLong tickCounter = new AtomicLong();
    private final AtomicReference<MetricsSnapshot> snapshot = new AtomicReference<>();
    private volatile double currentMspt = 50.0;
    private volatile double currentTps = 20.0;
    private long lastTickNanos = System.nanoTime();
    private int tickTaskId = -1;
    private int metricsTaskId = -1;

    public PerformanceMonitor(TickScope plugin) { this.plugin = plugin; }

    public void start() {
        if (tickTaskId != -1) return;
        lastTickNanos = System.nanoTime();
        tickTaskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.nanoTime();
            long elapsed = now - lastTickNanos;
            lastTickNanos = now;
            if (elapsed > 0) {
                double mspt = Math.max(0.01, elapsed / 1_000_000.0);
                currentMspt = mspt;
                currentTps = Math.min(20.0, 1000.0 / mspt);
            }
            tickCounter.incrementAndGet();
        }, 1L, 1L).getTaskId();

        long interval = Math.max(20L, plugin.getConfig().getLong("monitor.metrics-interval-ms", 1000L) / 50L);
        metricsTaskId = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshSnapshot, 1L, interval).getTaskId();
    }

    private void refreshSnapshot() {
        int players = Bukkit.getOnlinePlayers().size();
        int chunks = 0;
        int entities = 0;
        int tiles = 0;
        for (World world : Bukkit.getWorlds()) {
            Chunk[] loaded = world.getLoadedChunks();
            chunks += loaded.length;
            for (Chunk chunk : loaded) {
                entities += chunk.getEntities().length;
                tiles += chunk.getTileEntities().length;
            }
        }
        long uptime = Math.max(0L, (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L);
        snapshot.set(new MetricsSnapshot(System.currentTimeMillis(), tickCounter.get(), currentTps, currentMspt,
                players, chunks, entities, tiles, uptime));
    }

    public MetricsSnapshot collect() {
        MetricsSnapshot current = snapshot.get();
        if (current != null) return current;
        return new MetricsSnapshot(
                System.currentTimeMillis(),
                tickCounter.get(),
                currentTps,
                currentMspt,
                0,
                0,
                0,
                0,
                Math.max(0L, (System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()) / 1000L)
        );
    }

    public double getCurrentMspt() { return currentMspt; }
    public double getCurrentTps() { return currentTps; }
    public long getTickCounter() { return tickCounter.get(); }

    public double getCpuUsage() {
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) {
            double load = os.getCpuLoad();
            return load >= 0 ? load * 100.0 : -1.0;
        }
        return -1.0;
    }

    public void stop() {
        if (metricsTaskId != -1) { Bukkit.getScheduler().cancelTask(metricsTaskId); metricsTaskId = -1; }
        if (tickTaskId != -1) { Bukkit.getScheduler().cancelTask(tickTaskId); tickTaskId = -1; }
    }
}
