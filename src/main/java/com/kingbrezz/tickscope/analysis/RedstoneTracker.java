package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public final class RedstoneTracker implements Listener {
    private final TickScope plugin;
    private final Map<String, Counter> activity = new ConcurrentHashMap<>();

    public RedstoneTracker(TickScope plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent event) { record(event.getBlock()); }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) record(block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) record(block);
    }

    private void record(Block block) {
        if (!plugin.getConfig().getBoolean("analysis.redstone", true)) return;
        Location l = block.getLocation();
        String key = l.getWorld().getName() + ":" + l.getBlockX() + ":" +
                l.getBlockY() + ":" + l.getBlockZ();
        activity.computeIfAbsent(key, k -> new Counter(l.getWorld().getName(),
                l.getBlockX(), l.getBlockY(), l.getBlockZ())).increment();
    }

    public List<RedstoneActivity> getTopActivity(int limit) {
        long now = System.currentTimeMillis();
        List<RedstoneActivity> result = new ArrayList<>();
        for (Counter c : activity.values()) {
            result.add(new RedstoneActivity(c.world, c.x, c.y, c.z, c.count,
                    findNearestPlayer(c.world, c.x, c.y, c.z), now));
        }
        result.sort(Comparator.comparingLong(RedstoneActivity::activity).reversed());
        if (result.size() > limit) result = result.subList(0, limit);
        return List.copyOf(result);
    }

    private String findNearestPlayer(String worldName, int x, int y, int z) {
        if (!plugin.getConfig().getBoolean("hotspots.track-nearby-player", true)) return null;
        var world = plugin.getServer().getWorld(worldName);
        if (world == null) return null;
        Location target = new Location(world, x + .5, y + .5, z + .5);
        double best = Double.MAX_VALUE; String name = null;
        for (var player : world.getPlayers()) {
            double d = player.getLocation().distanceSquared(target);
            if (d < best) { best = d; name = player.getName(); }
        }
        return name;
    }

    public void clear() { activity.clear(); }

    private static final class Counter {
        final String world; final int x, y, z; long count;
        Counter(String world, int x, int y, int z) {
            this.world = world; this.x = x; this.y = y; this.z = z;
        }
        synchronized void increment() { count++; }
    }
}
