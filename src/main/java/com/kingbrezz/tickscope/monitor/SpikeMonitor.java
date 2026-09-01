package com.kingbrezz.tickscope.monitor;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

public final class SpikeMonitor {

    private final TickScope plugin;
    private final PerformanceMonitor monitor;
    private final SpikeHistory history;

    private int taskId = -1;

    public SpikeMonitor(
            TickScope plugin,
            PerformanceMonitor monitor
    ) {
        this.plugin = plugin;
        this.monitor = monitor;
        this.history = new SpikeHistory();
    }

    public void start() {

        long intervalTicks =
                Math.max(
                        1L,
                        plugin.getConfig()
                                .getLong(
                                        "spikes.interval-ms",
                                        1000L
                                ) / 50L
                );

        taskId =
                Bukkit.getScheduler()
                        .runTaskTimer(
                                plugin,
                                this::check,
                                intervalTicks,
                                intervalTicks
                        )
                        .getTaskId();
    }

    private void check() {

        MetricsSnapshot snapshot =
                monitor.collect();

        double tps = snapshot.tps();
        double mspt = snapshot.mspt();

        double warningMspt =
                plugin.getConfig()
                        .getDouble(
                                "spikes.warning-mspt",
                                50.0
                        );

        double criticalMspt =
                plugin.getConfig()
                        .getDouble(
                                "spikes.critical-mspt",
                                100.0
                        );

        String severity;

        if (mspt >= criticalMspt || tps < 15.0) {

            severity = "CRITICAL";

        } else if (
                mspt >= warningMspt ||
                tps < 18.0
        ) {

            severity = "WARNING";

        } else {

            return;
        }

        history.add(
                new SpikeEvent(
                        System.currentTimeMillis(),
                        tps,
                        mspt,
                        severity
                )
        );
    }

    public SpikeHistory getHistory() {
        return history;
    }

    public void clear() {
        history.clear();
    }

    public void stop() {

        if (taskId != -1) {

            Bukkit.getScheduler()
                    .cancelTask(taskId);

            taskId = -1;
        }
    }
}
