package com.kingbrezz.tickscope.analysis;

public record LagHotspot(
        HotspotType type,
        String world,
        int x,
        int y,
        int z,
        double score,
        long activity,
        String nearestPlayer
) {

    public String location() {
        return "X:" + x + " Y:" + y + " Z:" + z;
    }

    public String chunk() {
        return (x >> 4) + "," + (z >> 4);
    }
}
