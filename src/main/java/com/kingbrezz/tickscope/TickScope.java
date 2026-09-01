package com.kingbrezz.tickscope;

import org.bukkit.plugin.java.JavaPlugin;

public final class TickScope extends JavaPlugin {

    private static TickScope instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        getLogger().info("=================================");
        getLogger().info(" TickScope 1.0.0");
        getLogger().info(" Server Performance Monitor");
        getLogger().info(" Author: KingBrezz");
        getLogger().info("=================================");
        getLogger().info("TickScope enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TickScope disabled.");
        instance = null;
    }

    public static TickScope getInstance() {
        return instance;
    }
}
