package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SpikeDetector {

    private final TickScope plugin;
    private final PerformanceMonitor monitor;

    private final List<SpikeEvent> history =
            new CopyOnWriteArrayList<>();

    private boolean running;

    public SpikeDetector(
            TickScope plugin,
            PerformanceMonitor monitor
    ) {
        this.plugin = plugin;
        this.monitor = monitor;
    }

    public void start() {

        if (running) {
            return;
        }

        running = true;

        long intervalTicks =
                Math.max(
                        1L,
                        plugin.getConfig()
                                .getLong("monitor.interval-ms", 1000L)
                                / 50L
                );

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::check,
                intervalTicks,
                intervalTicks
        );
    }

    private void check() {

        if (!plugin.getConfig()
                .getBoolean("monitor.enabled", true)) {
            return;
        }

        double mspt = monitor.getCurrentMspt();

        double threshold =
                plugin.getConfig()
                        .getDouble(
                                "thresholds.spike-mspt",
                                50.0
                        );

        if (mspt < threshold) {
            return;
        }

        MetricsSnapshot snapshot =
                monitor.collect();

        String severity;

        if (mspt >= 100.0) {
            severity = "CRITICAL";
        } else if (mspt >= 75.0) {
            severity = "HIGH";
        } else {
            severity = "WARNING";
        }

        SpikeEvent event = new SpikeEvent(
                snapshot.timestamp(),
                snapshot.tick(),
                snapshot.mspt(),
                snapshot.tps(),
                snapshot.players(),
                snapshot.loadedChunks(),
                snapshot.entities(),
                snapshot.tileEntities(),
                severity
        );

        history.add(event);

        int maxHistory =
                plugin.getConfig()
                        .getInt("history.max-spikes", 1000);

        while (history.size() > maxHistory) {
            history.remove(0);
        }

        plugin.getLogger().warning(
                String.format(
                        "Tick spike detected: %.2f ms | TPS %.2f | Players %d | Severity %s",
                        event.mspt(),
                        event.tps(),
                        event.players(),
                        event.severity()
                )
        );
    }

    public List<SpikeEvent> getHistory() {
        return List.copyOf(history);
    }

    public SpikeEvent getLatest() {

        if (history.isEmpty()) {
            return null;
        }

        return history.get(history.size() - 1);
    }

    public void stop() {
        running = false;
    }
}
