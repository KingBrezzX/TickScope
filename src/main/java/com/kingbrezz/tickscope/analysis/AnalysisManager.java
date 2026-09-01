package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

import java.util.List;

public final class AnalysisManager {

    private final TickScope plugin;

    private final RedstoneTracker redstoneTracker;
    private final HotspotAnalyzer hotspotAnalyzer;

    public AnalysisManager(TickScope plugin) {
        this.plugin = plugin;

        this.redstoneTracker =
                new RedstoneTracker(plugin);

        this.hotspotAnalyzer =
                new HotspotAnalyzer(plugin);

        Bukkit.getPluginManager().registerEvents(
                redstoneTracker,
                plugin
        );
    }

    public void start() {

        long intervalTicks = Math.max(
                20L,
                plugin.getConfig()
                        .getLong(
                                "monitor.interval-ms",
                                1000L
                        ) / 50L
        );

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {

                    if (!plugin.getConfig()
                            .getBoolean(
                                    "analysis.redstone",
                                    true
                            )) {
                        return;
                    }

                    int max =
                            plugin.getConfig()
                                    .getInt(
                                            "hotspots.max-results",
                                            50
                                    );

                    redstoneTracker
                            .getTopActivity(max);

                },
                intervalTicks,
                intervalTicks
        );
    }

    public RedstoneTracker getRedstoneTracker() {
        return redstoneTracker;
    }

    public HotspotAnalyzer getHotspotAnalyzer() {
        return hotspotAnalyzer;
    }

    public List<RedstoneActivity> getRedstoneHotspots() {
        return redstoneTracker.getTopActivity(
                plugin.getConfig()
                        .getInt(
                                "hotspots.max-results",
                                50
                        )
        );
    }
}
