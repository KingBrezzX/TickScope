package com.kingbrezz.tickscope.ai;

public record Recommendation(
        String severity,
        String cause,
        String recommendation,
        double confidence
) {
}
