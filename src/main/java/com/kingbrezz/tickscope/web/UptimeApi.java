package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UptimeApi {

    private UptimeApi() {
    }

    public static Map<String, Object> get(
            TickScope plugin
    ) {

        long uptime =
                java.lang.management.ManagementFactory
                        .getRuntimeMXBean()
                        .getUptime();

        long seconds =
                uptime / 1000L;

        long days =
                seconds / 86400L;

        seconds %= 86400L;

        long hours =
                seconds / 3600L;

        seconds %= 3600L;

        long minutes =
                seconds / 60L;

        seconds %= 60L;

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "uptimeSeconds",
                uptime / 1000L
        );

        result.put(
                "formatted",
                String.format(
                        "%dd %02dh %02dm %02ds",
                        days,
                        hours,
                        minutes,
                        seconds
                )
        );

        result.put(
                "onlinePlayers",
                plugin.getServer()
                        .getOnlinePlayers()
                        .size()
        );

        return result;
    }
}
