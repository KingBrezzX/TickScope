package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.monitor.MetricsSnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RealtimeSnapshot {

    private RealtimeSnapshot() {
    }

    public static Map<String, Object> create(
            TickScope plugin
    ) {

        MetricsSnapshot snapshot =
                plugin.getPerformanceMonitor()
                        .collect();

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "type",
                "metrics"
        );

        data.put(
                "timestamp",
                snapshot.timestamp()
        );

        data.put(
                "tick",
                snapshot.tick()
        );

        data.put(
                "tps",
                snapshot.tps()
        );

        data.put(
                "mspt",
                snapshot.mspt()
        );

        data.put(
                "players",
                snapshot.players()
        );

        data.put(
                "loadedChunks",
                snapshot.loadedChunks()
        );

        data.put(
                "entities",
                snapshot.entities()
        );

        data.put(
                "tileEntities",
                snapshot.tileEntities()
        );

        data.put(
                "uptimeSeconds",
                snapshot.uptimeSeconds()
        );

        return data;
    }
}
