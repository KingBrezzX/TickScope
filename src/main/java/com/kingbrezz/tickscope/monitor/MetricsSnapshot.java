package com.kingbrezz.tickscope.monitor;

public record MetricsSnapshot(
        long timestamp,
        long tick,
        double tps,
        double mspt,
        int players,
        int loadedChunks,
        int entities,
        int tileEntities,
        long uptimeSeconds
) {
}
