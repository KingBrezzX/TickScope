package com.kingbrezz.tickscope.monitor;

public record SpikeEvent(
        long timestamp,
        double tps,
        double mspt,
        String severity
) {
}
