package com.kingbrezz.tickscope.history;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

public final class HistoryScheduler {

    private final TickScope plugin;
    private final HistoryManager historyManager;

    public HistoryScheduler(
            TickScope plugin,
            HistoryManager historyManager
    ) {
        this.plugin = plugin;
        this.historyManager = historyManager;
    }

    public void start() {

        long interval =
                Math.max(
                        1L,
                        plugin.getConfig().getLong(
                                "history.cleanup-interval-hours",
                                168
                        )
                );

        long ticks =
                interval * 60L * 60L * 20L;

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                historyManager::cleanup,
                ticks,
                ticks
        );
    }
}
