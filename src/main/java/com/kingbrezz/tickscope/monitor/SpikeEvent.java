package com.kingbrezz.tickscope.monitor;

public record SpikeEvent(
        long timestamp,
        double tps,
        double mspt,
        int players,
        int loadedChunks,
        int entities,
        int tileEntities,
        String severity
) {}
