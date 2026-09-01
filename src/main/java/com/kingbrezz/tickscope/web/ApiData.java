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

        return data;
    }

    public static Map<String, Object> all(TickScope plugin) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("data", status(plugin));
        result.put("spikes", SpikeApi.get(plugin));
        result.put("hotspots", hotspots(plugin));
        result.put("redstone", redstone(plugin));
        result.put("entities", entities(plugin));
        result.put("tileEntities", tileEntities(plugin));
        result.put("recommendations", recommendations(plugin));
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public static List<RedstoneActivity> redstone(
            TickScope plugin
    ) {

        return plugin.getAnalysisManager()
                .getRedstoneHotspots();
    }

    public static List<EntityHotspot> entities(
            TickScope plugin
    ) {

        return plugin.getAnalysisManager().getEntities();
    }

    public static List<LagHotspot> tileEntities(
            TickScope plugin
    ) {

        return plugin.getAnalysisManager().getTiles();
    }

    public static List<LagHotspot> hotspots(
            TickScope plugin
    ) {

        return plugin.getAnalysisManager().getHotspots();
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
