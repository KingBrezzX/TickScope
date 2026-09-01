package com.kingbrezz.tickscope.history;

public record HistoryEntry(
        long timestamp,
        String type,
        String world,
        int x,
        int y,
        int z,
        double score,
        String player,
        String details
) {
}
