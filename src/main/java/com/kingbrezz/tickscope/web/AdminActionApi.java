package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

import java.util.Map;

public final class AdminActionApi {
    private AdminActionApi() {}

    public static Map<String, Object> destroy(TickScope plugin, String world, int x, int y, int z,
                                                String player, boolean autoBan) {
        return plugin.getAdminActionManager().destroyRedstoneCore(world, x, y, z, player, autoBan);
    }

    public static Map<String, Object> ban(TickScope plugin, String player) {
        return plugin.getAdminActionManager().tempBan(player);
    }
}
