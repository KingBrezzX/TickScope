package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.block.BlockState;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.atomic.AtomicLong;

public final class PerformanceMonitor {

    private final TickScope plugin;
    private final AtomicLong tickCounter = new AtomicLong();

    private volatile double currentMspt = 0.0;
    private volatile double currentTps = 20.0;

    private long lastTickNanos = System.nanoTime();

    public PerformanceMonitor(TickScope plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            long now = System.nanoTime();

            long elapsed = now - lastTickNanos;
            lastTickNanos = now;

            double mspt = elapsed / 1_000_000.0;

            if (mspt > 0) {
                currentMspt = mspt;
                currentTps = Math.min(20.0, 1000.0 / mspt);
            }

            tickCounter.incrementAndGet();

        }, 1L, 1L);
    }

    public MetricsSnapshot collect() {
        int players = Bukkit.getOnlinePlayers().size();

        int chunks = 0;
        int entities = 0;
        int tiles = 0;

        for (World world : Bukkit.getWorlds()) {

            chunks += world.getLoadedChunks().length;

            for (Chunk chunk : world.getLoadedChunks()) {

                entities += chunk.getEntities().length;

                for (BlockState state : chunk.getTileEntities()) {
                    tiles++;
                }
            }
        }

        long uptime =
                (System.currentTimeMillis() -
                        ManagementFactory.getRuntimeMXBean().getStartTime())
                        / 1000L;

        return new MetricsSnapshot(
                System.currentTimeMillis(),
                tickCounter.get(),
                currentTps,
                currentMspt,
                players,
                chunks,
                entities,
                tiles,
                uptime
        );
    }

    public double getCurrentMspt() {
        return currentMspt;
    }

    public double getCurrentTps() {
        return currentTps;
    }

    public long getTickCounter() {
        return tickCounter.get();
    }

    public double getCpuUsage() {
        OperatingSystemMXBean os =
                ManagementFactory.getOperatingSystemMXBean();

        if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
            double cpu = sunOs.getCpuLoad();

            if (cpu >= 0) {
                return cpu * 100.0;
            }
        }

        return -1.0;
    }
}
