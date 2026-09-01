package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

public final class RedstoneTracker implements Listener {

    private final TickScope plugin;

    private final Map<String, Counter> activity =
            new ConcurrentHashMap<>();

    public RedstoneTracker(TickScope plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent event) {
        record(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            record(block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            record(block);
        }
    }

    private void record(Block block) {

        if (!plugin.getConfig()
                .getBoolean("analysis.redstone", true)) {
            return;
        }

        Location location = block.getLocation();

        String key =
                location.getWorld().getName()
                        + ":"
                        + location.getBlockX()
                        + ":"
                        + location.getBlockY()
                        + ":"
                        + location.getBlockZ();

        activity.computeIfAbsent(
                key,
                ignored -> new Counter(
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ()
                )
        ).increment();
    }

    public List<RedstoneActivity> getTopActivity(int limit) {

        List<RedstoneActivity> result = new ArrayList<>();

        long now = System.currentTimeMillis();

        for (Counter counter : activity.values()) {

            String player =
                    findNearestPlayer(
                            counter.world,
                            counter.x,
                            counter.y,
                            counter.z
                    );

            result.add(new RedstoneActivity(
                    counter.world,
                    counter.x,
                    counter.y,
                    counter.z,
                    counter.count,
                    player,
                    now
            ));
        }

        result.sort(
                Comparator.comparingLong(
                        RedstoneActivity::activity
                ).reversed()
        );

        if (result.size() > limit) {
            return List.copyOf(
                    result.subList(0, limit)
            );
        }

        return List.copyOf(result);
    }

    private String findNearestPlayer(
            String worldName,
            int x,
            int y,
            int z
    ) {

        if (!plugin.getConfig()
                .getBoolean(
                        "hotspots.track-nearby-player",
                        true
                )) {
            return null;
        }

        if (plugin.getServer()
                .getWorld(worldName) == null) {
            return null;
        }

        double best = Double.MAX_VALUE;
        String name = null;

        Location target =
                new Location(
                        plugin.getServer().getWorld(worldName),
                        x + 0.5,
                        y + 0.5,
                        z + 0.5
                );

        for (org.bukkit.entity.Player player :
                target.getWorld().getPlayers()) {

            double distance =
                    player.getLocation()
                            .distanceSquared(target);

            if (distance < best) {
                best = distance;
                name = player.getName();
            }
        }

        return name;
    }

    public void clear() {
        activity.clear();
    }

    private static final class Counter {

        private final String world;
        private final int x;
        private final int y;
        private final int z;

        private long count;

        private Counter(
                String world,
                int x,
                int y,
                int z
        ) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private synchronized void increment() {
            count++;
        }
    }
          }
