package com.kingbrezz.tickscope.admin;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AdminActionManager {
    private final TickScope plugin;

    public AdminActionManager(TickScope plugin) { this.plugin = plugin; }

    public Map<String, Object> destroyRedstoneCore(String worldName, int x, int y, int z,
                                                     String playerName, boolean autoBan) {
        if (!plugin.getConfig().getBoolean("admin-actions.enabled", true)) {
            throw new IllegalStateException("Admin actions are disabled in config.");
        }
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) throw new IllegalArgumentException("World not found: " + worldName);

        int radius = Math.max(0, plugin.getConfig().getInt("admin-actions.break.radius", 0));
        Location core = new Location(world, x, y, z);
        int broken = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    if (block.getType().isAir()) continue;
                    block.setType(Material.AIR, false);
                    broken++;
                }
            }
        }

        if (plugin.getConfig().getBoolean("admin-actions.break.replace-core-with-sign", true)) {
            placeConfiguredSign(core);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("action", "destroy-redstone-core");
        result.put("world", worldName);
        result.put("x", x);
        result.put("y", y);
        result.put("z", z);
        result.put("radius", radius);
        result.put("blocksBroken", broken);
        result.put("signPlaced", plugin.getConfig().getBoolean("admin-actions.break.replace-core-with-sign", true));

        boolean banned = false;
        String bannedPlayer = null;
        if (autoBan && plugin.getConfig().getBoolean("admin-actions.ban.enabled", true)
                && playerName != null && !playerName.isBlank()) {
            banned = tempBanInternal(playerName.trim());
            if (banned) bannedPlayer = playerName.trim();
        }
        result.put("banned", banned);
        result.put("player", bannedPlayer);
        return result;
    }

    public Map<String, Object> tempBan(String playerName) {
        if (!plugin.getConfig().getBoolean("admin-actions.enabled", true) ||
                !plugin.getConfig().getBoolean("admin-actions.ban.enabled", true)) {
            return Map.of("action", "tempban", "player", playerName, "success", false, "error", "Tempban is disabled in config");
        }
        boolean success = tempBanInternal(playerName);
        return Map.of(
                "action", "tempban",
                "player", playerName,
                "success", success,
                "durationDays", Math.max(1, plugin.getConfig().getInt("admin-actions.ban.duration-days", 7)),
                "reason", plugin.getConfig().getString("admin-actions.ban.reason", "Lag machine detected")
        );
    }

    private boolean tempBanInternal(String playerName) {
        if (playerName == null || playerName.isBlank()) return false;
        int days = Math.max(1, plugin.getConfig().getInt("admin-actions.ban.duration-days", 7));
        String reason = plugin.getConfig().getString("admin-actions.ban.reason", "Lag machine detected");
        Duration duration = Duration.ofDays(days);

        Player online = plugin.getServer().getPlayerExact(playerName);
        if (online != null) {
            online.ban(reason, duration, "TickScope", true);
            return true;
        }

        var offline = plugin.getServer().getOfflinePlayer(playerName);
        offline.ban(reason, duration, "TickScope");
        return true;
    }

    private void placeConfiguredSign(Location location) {
        Block block = location.getBlock();
        block.setType(Material.OAK_SIGN, false);
        if (!(block.getState() instanceof Sign sign)) return;
        var lines = plugin.getConfig().getStringList("admin-actions.sign.lines");
        for (int i = 0; i < 4; i++) sign.setLine(i, i < lines.size() ? ChatColor.translateAlternateColorCodes('&', lines.get(i)) : "");
        sign.update(true, false);
    }
}

