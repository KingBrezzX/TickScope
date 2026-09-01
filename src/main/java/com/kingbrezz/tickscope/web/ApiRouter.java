package com.kingbrezz.tickscope.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingbrezz.tickscope.TickScope;

import java.util.Map;

public final class ApiRouter {

    private final TickScope plugin;
    private final Gson gson =
            new GsonBuilder().create();

    public ApiRouter(TickScope plugin) {
        this.plugin = plugin;
    }

    public String route(String path) {

        if (path.equals("/api/status")) {

            return json(
                    ApiData.status(plugin)
            );
        }

        if (path.equals("/api/server")) {

            return json(
                    ApiUtil.serverInfo(plugin)
            );
        }

        if (path.equals("/api/redstone")) {

            return json(
                    ApiData.redstone(plugin)
            );
        }

        if (path.equals("/api/entities")) {

            return json(
                    ApiData.entities(plugin)
            );
        }

        if (path.equals("/api/tile-entities")) {

            return json(
                    ApiData.tileEntities(plugin)
            );
        }

        if (path.equals("/api/hotspots")) {

            return json(
                    ApiData.hotspots(plugin)
            );
        }

        if (path.equals("/api/recommendations")) {

            return json(
                    ApiData.recommendations(plugin)
            );
        }

        return json(
                Map.of(
                        "error",
                        "Endpoint not found"
                )
        );
    }

    private String json(Object object) {
        return gson.toJson(object);
    }
}
