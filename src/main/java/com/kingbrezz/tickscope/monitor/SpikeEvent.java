package com.kingbrezz.tickscope.monitor;

public record SpikeEvent(
        long timestamp,
        long tick,
        double mspt,
        double tps,
        int players,
        int loadedChunks,
        int entities,
        int tileEntities,
        String severity
) {

    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(severity);
    }
}
