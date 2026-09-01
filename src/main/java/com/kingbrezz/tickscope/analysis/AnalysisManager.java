package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;
import java.util.List;

public final class AnalysisManager {
    private final TickScope plugin;
    private final RedstoneTracker redstoneTracker;
    private final HotspotAnalyzer hotspotAnalyzer;
    private volatile List<LagHotspot> cachedHotspots = List.of();
    private volatile List<EntityHotspot> cachedEntities = List.of();
    private volatile List<LagHotspot> cachedTiles = List.of();
    private int taskId = -1;

    public AnalysisManager(TickScope plugin) {
        this.plugin = plugin;
        this.redstoneTracker = new RedstoneTracker(plugin);
        this.hotspotAnalyzer = new HotspotAnalyzer(plugin);
        Bukkit.getPluginManager().registerEvents(redstoneTracker, plugin);
    }

    public void start() {
        if (taskId != -1) return;
        long interval = Math.max(20L, plugin.getConfig().getLong("hotspots.scan-interval-ms", 5000L) / 50L);
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, interval, interval).getTaskId();
    }

    private void refresh() {
        if (plugin.getConfig().getBoolean("hotspots.enabled", true)) {
            cachedHotspots = hotspotAnalyzer.scan();
        } else cachedHotspots = List.of();
        if (plugin.getConfig().getBoolean("analysis.entities", true)) {
            cachedEntities = plugin.getEntityAnalyzer().scan();
        } else cachedEntities = List.of();
        if (plugin.getConfig().getBoolean("analysis.tile-entities", true)) {
            cachedTiles = plugin.getTileEntityAnalyzer().scan();
        } else cachedTiles = List.of();
    }

    public RedstoneTracker getRedstoneTracker() { return redstoneTracker; }
    public HotspotAnalyzer getHotspotAnalyzer() { return hotspotAnalyzer; }
    public List<RedstoneActivity> getRedstoneHotspots() {
        return redstoneTracker.getTopActivity(plugin.getConfig().getInt("hotspots.max-results", 50));
    }
    public List<LagHotspot> getHotspots() { return cachedHotspots; }
    public List<EntityHotspot> getEntities() { return cachedEntities; }
    public List<LagHotspot> getTiles() { return cachedTiles; }
    public void stop() { if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; } }
}
