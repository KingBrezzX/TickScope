package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.ai.Recommendation;
import com.kingbrezz.tickscope.analysis.EntityHotspot;
import com.kingbrezz.tickscope.analysis.LagHotspot;
import com.kingbrezz.tickscope.analysis.RedstoneActivity;
import com.kingbrezz.tickscope.monitor.MetricsSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApiData {

    private ApiData() {
    }

    public static Map<String, Object> status(TickScope plugin) {
        MetricsSnapshot snapshot =
                plugin.getPerformanceMonitor().collect();

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put("timestamp", snapshot.timestamp());
        data.put("tick", snapshot.tick());
        data.put("tps", snapshot.tps());
        data.put("mspt", snapshot.mspt());
        data.put("players", snapshot.players());
        data.put("loadedChunks", snapshot.loadedChunks());
        data.put("entities", snapshot.entities());
        data.put("tileEntities", snapshot.tileEntities());
        data.put("uptimeSeconds", snapshot.uptimeSeconds());
        data.put("online", true);
        data.put("server", plugin.getServer().getName());
        data.put("minecraft", plugin.getServer().getMinecraftVersion());

        return data;
    }

    /**
     * Single endpoint for the public dashboard.
     *
     * /api/v1/all -> this method through TickScopeWebServer.
     */
    public static Map<String, Object> all(TickScope plugin) {
        MetricsSnapshot snapshot =
                plugin.getPerformanceMonitor().collect();

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put("timestamp", snapshot.timestamp());
        data.put("tick", snapshot.tick());
        data.put("tps", snapshot.tps());
        data.put("mspt", snapshot.mspt());
        data.put("players", snapshot.players());
        data.put("loadedChunks", snapshot.loadedChunks());
        data.put("entities", snapshot.entities());
        data.put("tileEntities", snapshot.tileEntities());
        data.put("uptimeSeconds", snapshot.uptimeSeconds());
        data.put("online", true);
        data.put("server",
                plugin.getConfig().getString(
                        "server.name",
                        plugin.getServer().getName()
                ));
        data.put("minecraft",
                plugin.getServer().getMinecraftVersion());

        // Keep all detailed dashboard data in one request.
        data.put("spikes", SpikeApi.get(plugin));
        data.put("hotspots", hotspots(plugin));
        data.put("redstone", redstone(plugin));
        data.put("entityHotspots", entities(plugin));
        data.put("tileEntityHotspots", tileEntities(plugin));
        data.put("recommendations", recommendations(plugin));

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("success", true);
        result.put("data", data);
        result.put("timestamp",
                System.currentTimeMillis());

        return result;
    }

    public static List<RedstoneActivity> redstone(TickScope plugin) {
        return plugin.getAnalysisManager()
                .getRedstoneHotspots();
    }

    public static List<EntityHotspot> entities(TickScope plugin) {
        return plugin.getAnalysisManager()
                .getEntities();
    }

    public static List<LagHotspot> tileEntities(TickScope plugin) {
        return plugin.getAnalysisManager()
                .getTiles();
    }

    public static List<LagHotspot> hotspots(TickScope plugin) {
        return plugin.getAnalysisManager()
                .getHotspots();
    }

    public static List<Recommendation> recommendations(
            TickScope plugin
    ) {
        MetricsSnapshot snapshot =
                plugin.getPerformanceMonitor()
                        .collect();

        return plugin.getRecommendationEngine()
                .analyze(snapshot);
    }
}
