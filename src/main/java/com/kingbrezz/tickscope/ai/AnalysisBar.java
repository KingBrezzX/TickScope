package com.kingbrezz.tickscope.ai;

public final class AnalysisBar {

    private AnalysisBar() {
    }

    public static String create(
            double value,
            double warning,
            double critical,
            int length
    ) {

        length = Math.max(5, length);

        double ratio;

        if (critical <= 0) {
            ratio = 0.0;
        } else {
            ratio = value / critical;
        }

        ratio = Math.max(
                0.0,
                Math.min(1.0, ratio)
        );

        int filled =
                (int) Math.round(
                        ratio * length
                );

        StringBuilder bar =
                new StringBuilder();

        for (int i = 0; i < length; i++) {

            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }

        String severity;

        if (value >= critical) {
            severity = "CRITICAL";
        } else if (value >= warning) {
            severity = "WARNING";
        } else {
            severity = "HEALTHY";
        }

        return bar
                + " "
                + String.format(
                        "%.1f",
                        value
                )
                + " [" + severity + "]";
    }
}
