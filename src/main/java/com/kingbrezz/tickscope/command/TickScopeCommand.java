package com.kingbrezz.tickscope.command;

import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.monitor.MetricsSnapshot;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class TickScopeCommand implements CommandExecutor {
    private final TickScope plugin;
    public TickScopeCommand(TickScope plugin) { this.plugin = plugin; }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("tickscope.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (command.getName().equalsIgnoreCase("token")) {
            sender.sendMessage(ChatColor.AQUA + "TickScope Server Token");
            sender.sendMessage(ChatColor.WHITE + plugin.getTokenManager().getToken());
            sender.sendMessage(ChatColor.GRAY + "Keep this token private.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("token")) {
            sender.sendMessage(ChatColor.AQUA + "Token: " + ChatColor.WHITE + plugin.getTokenManager().getToken());
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("destroy") && args.length >= 5) {
            try {
                String world = args[1];
                int x = Integer.parseInt(args[2]);
                int y = Integer.parseInt(args[3]);
                int z = Integer.parseInt(args[4]);
                String player = args.length >= 6 ? args[5] : null;
                boolean autoBan = args.length >= 7 && Boolean.parseBoolean(args[6]);
                var result = plugin.getAdminActionManager().destroyRedstoneCore(world, x, y, z, player, autoBan);
                sender.sendMessage(ChatColor.GREEN + "Lag machine destroyed. Blocks broken: " + result.get("blocksBroken"));
                sender.sendMessage(ChatColor.GRAY + "Sign placed: " + result.get("signPlaced") + ", banned: " + result.get("banned"));
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Destroy failed: " + e.getMessage());
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("ban") && args.length >= 2) {
            var result = plugin.getAdminActionManager().tempBan(args[1]);
            sender.sendMessage(Boolean.TRUE.equals(result.get("success"))
                    ? ChatColor.GREEN + "Tempbanned " + args[1] + " for " + result.get("durationDays") + " days."
                    : ChatColor.RED + "Tempban failed.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            MetricsSnapshot d = plugin.getPerformanceMonitor().collect();
            sender.sendMessage(ChatColor.AQUA + "===== TickScope =====");
            sender.sendMessage(ChatColor.WHITE + "TPS: " + ChatColor.GREEN + String.format("%.2f", d.tps()));
            sender.sendMessage(ChatColor.WHITE + "MSPT: " + ChatColor.GREEN + String.format("%.2f", d.mspt()));
            sender.sendMessage(ChatColor.WHITE + "Players: " + ChatColor.YELLOW + d.players());
            sender.sendMessage(ChatColor.WHITE + "Chunks: " + ChatColor.YELLOW + d.loadedChunks());
            sender.sendMessage(ChatColor.WHITE + "Entities: " + ChatColor.YELLOW + d.entities());
            sender.sendMessage(ChatColor.WHITE + "Tiles: " + ChatColor.YELLOW + d.tileEntities());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadRuntime();
            sender.sendMessage(ChatColor.GREEN + "TickScope configuration and runtime services reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "/tickscope status");
        sender.sendMessage(ChatColor.YELLOW + "/tickscope token");
        sender.sendMessage(ChatColor.YELLOW + "/tickscope reload");
        sender.sendMessage(ChatColor.YELLOW + "/token");
        sender.sendMessage(ChatColor.YELLOW + "/tickscope destroy <world> <x> <y> <z> [player] [true|false]");
        sender.sendMessage(ChatColor.YELLOW + "/tickscope ban <player>");
        return true;
    }
}
