package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

import java.util.List;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    public static List<String> all() {

        return List.of(
                "/api/health",
                "/api/server",
                "/api/status",
                "/api/hotspots",
                "/api/redstone",
                "/api/entities",
                "/api/tile-entities",
                "/api/recommendations",
                "/api/uptime",
                "/api/spikes",
                "/api/stream",
                "/api/admin/destroy",
                "/api/admin/ban"
        );
    }

    public static void register(
            TickScope plugin
    ) {

        plugin.getLogger().info(
                "Registered TickScope API endpoints:"
        );

        for (String endpoint : all()) {

            plugin.getLogger().info(
                    "  " + endpoint
            );
        }
    }
}
