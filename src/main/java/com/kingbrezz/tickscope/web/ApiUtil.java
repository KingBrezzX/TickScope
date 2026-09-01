package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiUtil {
    private ApiUtil() {}

    public static Map<String, Object> serverInfo(TickScope plugin) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plugin", "TickScope");
        data.put("version", plugin.getDescription().getVersion());
        data.put("server", plugin.getServer().getName());
        data.put("minecraft", plugin.getServer().getMinecraftVersion());
        data.put("java", System.getProperty("java.version"));
        data.put("onlinePlayers", plugin.getServer().getOnlinePlayers().size());
        data.put("worlds", plugin.getServer().getWorlds().size());
        data.put("serverIp", plugin.getConfig().getString("server.ip", "127.0.0.1"));
        data.put("serverPort", plugin.getConfig().getInt("server.port", 25565));
        data.put("webHost", plugin.getConfig().getString("web.host", "127.0.0.1"));
        data.put("webPort", plugin.getConfig().getInt("web.port", 8765));
        data.put("publicUrl", plugin.getConfig().getString("web.public-url", ""));
        data.put("serverName", plugin.getConfig().getString("server.name", plugin.getServer().getName()));
        data.put("apiOnline", true);
        data.put("paperVersion", plugin.getServer().getVersion());
        data.put("javaVersion", System.getProperty("java.version"));
        return data;
    }
}
