package com.kingbrezz.tickscope.analysis;

public record RedstoneActivity(
        String world,
        int x,
        int y,
        int z,
        long activity,
        String nearestPlayer,
        long timestamp
) {
}
