package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConnectionInfo {

    private ConnectionInfo() {
    }

    public static Map<String, Object> create(
            TickScope plugin
    ) {

        WebConfig config =
                new WebConfig(plugin);

        Map<String, Object> data =
                new LinkedHashMap<>();

        data.put(
                "host",
                config.getHost()
        );

        data.put(
                "port",
                config.getPort()
        );

        data.put(
                "authentication",
                config.isAuthenticationEnabled()
        );

        data.put(
                "realtime",
                config.isRealtimeEnabled()
        );

        data.put(
                "api",
                "/api"
        );

        data.put(
                "stream",
                "/api/stream"
        );

        return data;
    }
          }
