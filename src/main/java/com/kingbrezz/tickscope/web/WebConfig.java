package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

public final class WebConfig {

    private final TickScope plugin;

    public WebConfig(TickScope plugin) {
        this.plugin = plugin;
    }

    public String getHost() {
        return plugin.getConfig()
                .getString(
                        "web.host",
                        "127.0.0.1"
                );
    }

    public int getPort() {
        return plugin.getConfig()
                .getInt(
                        "web.port",
                        8765
                );
    }

    public boolean isEnabled() {
        return plugin.getConfig()
                .getBoolean(
                        "web.enabled",
                        true
                );
    }

    public boolean isAuthenticationEnabled() {
        return plugin.getConfig()
                .getBoolean(
                        "web.authentication.enabled",
                        true
                );
    }

    public boolean isRealtimeEnabled() {
        return plugin.getConfig()
                .getBoolean(
                        "web.realtime.enabled",
                        true
                );
    }
}
