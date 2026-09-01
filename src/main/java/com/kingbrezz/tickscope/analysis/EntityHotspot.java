package com.kingbrezz.tickscope.analysis;

public record EntityHotspot(
        String world,
        int x,
        int y,
        int z,
        String entityType,
        int count,
        double score,
        String nearestPlayer
) {
}
