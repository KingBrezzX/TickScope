package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

public final class WebConfig {

    private final TickScope plugin;

    public WebConfig(TickScope plugin) {
        this.plugin = plugin;
    }

    public String getHost() {
        return plugin.getConfig().getString("web.host", "127.0.0.1");
    }

    public int getPort() {
        return plugin.getConfig().getInt("web.port", 8765);
    }

    public String getPublicUrl() {
        return plugin.getConfig().getString("web.public-url", "");
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("web.enabled", true);
    }

    public boolean isCorsEnabled() {
        return plugin.getConfig().getBoolean("web.cors.enabled", true);
    }

    public boolean isAuthenticationEnabled() {
        return plugin.getConfig().getBoolean("web.authentication.enabled", true);
    }

    public boolean isAutoGenerateToken() {
        return plugin.getConfig().getBoolean("web.authentication.auto-generate-token", true);
    }

    public boolean isRealtimeEnabled() {
        return plugin.getConfig().getBoolean("web.realtime.enabled", true);
    }

    public int getRealtimeIntervalMs() {
        return plugin.getConfig().getInt("web.realtime.interval-ms", 1000);
    }

    public String getServerName() {
        return plugin.getConfig().getString("server.name", plugin.getServer().getName());
    }

    public String getServerIp() {
        return plugin.getConfig().getString("server.ip", "127.0.0.1");
    }

    public int getServerPort() {
        return plugin.getConfig().getInt("server.port", 25565);
    }
}
