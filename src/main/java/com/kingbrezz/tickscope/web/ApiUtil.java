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
        data.put("webPort", plugin.getConfig().getInt("web.port", 8765));
        return data;
    }
}
