package com.kingbrezz.tickscope.command;

import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.monitor.MetricsSnapshot;
import com.kingbrezz.tickscope.monitor.PerformanceMonitor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class TickScopeCommand implements CommandExecutor {

    private final TickScope plugin;

    public TickScopeCommand(TickScope plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission("tickscope.admin")) {
            sender.sendMessage(
                    ChatColor.RED + "You don't have permission."
            );
            return true;
        }

        PerformanceMonitor monitor = plugin.getPerformanceMonitor();

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {

            MetricsSnapshot data = monitor.collect();

            sender.sendMessage("");
            sender.sendMessage(
                    ChatColor.AQUA + "===== TickScope ====="
            );

            sender.sendMessage(
                    ChatColor.WHITE + "TPS: "
                            + ChatColor.GREEN
                            + String.format("%.2f", data.tps())
            );

            sender.sendMessage(
                    ChatColor.WHITE + "MSPT: "
                            + ChatColor.GREEN
                            + String.format("%.2f", data.mspt())
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Players: "
                            + ChatColor.YELLOW
                            + data.players()
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Loaded Chunks: "
                            + ChatColor.YELLOW
                            + data.loadedChunks()
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Entities: "
                            + ChatColor.YELLOW
                            + data.entities()
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Tile Entities: "
                            + ChatColor.YELLOW
                            + data.tileEntities()
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Tick: "
                            + ChatColor.YELLOW
                            + data.tick()
            );

            sender.sendMessage(
                    ChatColor.WHITE + "Uptime: "
                            + ChatColor.YELLOW
                            + formatUptime(data.uptimeSeconds())
            );

            sender.sendMessage("");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {

            plugin.reloadConfig();

            sender.sendMessage(
                    ChatColor.GREEN
                            + "TickScope configuration reloaded."
            );

            return true;
        }

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/tickscope status"
        );

        sender.sendMessage(
                ChatColor.YELLOW
                        + "/tickscope reload"
        );

        return true;
    }

    private String formatUptime(long seconds) {

        long days = seconds / 86400;
        seconds %= 86400;

        long hours = seconds / 3600;
        seconds %= 3600;

        long minutes = seconds / 60;
        seconds %= 60;

        return days + "d "
                + hours + "h "
                + minutes + "m "
                + seconds + "s";
    }
}
