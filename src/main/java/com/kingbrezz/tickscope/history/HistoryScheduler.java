package com.kingbrezz.tickscope.history;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

public final class HistoryScheduler {
    private final TickScope plugin;
    private final HistoryManager historyManager;
    private int taskId = -1;

    public HistoryScheduler(TickScope plugin, HistoryManager historyManager) {
        this.plugin = plugin; this.historyManager = historyManager;
    }

    public void start() {
        long hours = Math.max(1L, plugin.getConfig().getLong("history.cleanup-interval-hours", 24));
        long ticks = hours * 60L * 60L * 20L;
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, historyManager::cleanup, ticks, ticks).getTaskId();
    }

    public void stop() {
        if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; }
    }
}
