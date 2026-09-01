package com.kingbrezz.tickscope.ai;

import com.kingbrezz.tickscope.monitor.MetricsSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RecommendationEngine {

    public List<Recommendation> analyze(
            MetricsSnapshot snapshot
    ) {

        List<Recommendation> results =
                new ArrayList<>();

        double mspt = snapshot.mspt();
        double tps = snapshot.tps();

        if (mspt >= 100.0) {

            results.add(new Recommendation(
                    "CRITICAL",
                    "Extremely high server tick time",
                    "Immediately inspect the latest lag hotspots, entities, redstone activity and chunk loading.",
                    0.95
            ));

        } else if (mspt >= 50.0) {

            results.add(new Recommendation(
                    "HIGH",
                    "Server tick time is above the 20 TPS budget",
                    "Check the active lag hotspots and identify which subsystem is generating the workload.",
                    0.90
            ));

        } else if (mspt >= 40.0) {

            results.add(new Recommendation(
                    "WARNING",
                    "MSPT is approaching the performance limit",
                    "Monitor the server for spikes and investigate unusually busy chunks.",
                    0.82
            ));
        }

        if (tps < 18.0) {

            results.add(new Recommendation(
                    "HIGH",
                    "TPS degradation detected",
                    "Inspect recent spike events and correlate them with chunk, entity and redstone activity.",
                    0.88
            ));
        }

        if (snapshot.entities() > 500) {

            results.add(new Recommendation(
                    "WARNING",
                    "High entity population",
                    "Inspect entity-heavy chunks and remove unnecessary entity accumulation.",
                    0.76
            ));
        }

        if (snapshot.tileEntities() > 250) {

            results.add(new Recommendation(
                    "WARNING",
                    "High tile entity count",
                    "Inspect chunks containing large numbers of containers, machines or other tile entities.",
                    0.74
            ));
        }

        if (snapshot.loadedChunks() > 1500) {

            results.add(new Recommendation(
                    "WARNING",
                    "Large number of loaded chunks",
                    "Check player spread, view distance and chunk-loading activity.",
                    0.72
            ));
        }

        results.sort(
                Comparator.comparingDouble(
                        Recommendation::confidence
                ).reversed()
        );

        return List.copyOf(results);
    }
}
