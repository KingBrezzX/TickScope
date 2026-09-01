package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HotspotAnalyzer {

    private final TickScope plugin;

    public HotspotAnalyzer(TickScope plugin) {
        this.plugin = plugin;
    }

    public List<LagHotspot> scan() {

        List<LagHotspot> hotspots = new ArrayList<>();

        if (!plugin.getConfig().getBoolean("hotspots.enabled", true)) {
            return hotspots;
        }

        int maxResults = Math.max(
                1,
                plugin.getConfig().getInt("hotspots.max-results", 50)
        );

        int radius = Math.max(
                1,
                plugin.getConfig().getInt("hotspots.radius", 16)
        );

        for (World world : plugin.getServer().getWorlds()) {

            for (Chunk chunk : world.getLoadedChunks()) {

                Entity[] entities = chunk.getEntities();
                BlockState[] tiles = chunk.getTileEntities();

                if (entities.length > 0) {

                    Entity center = entities[0];

                    Player nearest = findNearestPlayer(
                            world,
                            center.getX(),
                            center.getY(),
                            center.getZ(),
                            radius
                    );

                    hotspots.add(new LagHotspot(
                            HotspotType.ENTITY,
                            world.getName(),
                            center.getLocation().getBlockX(),
                            center.getLocation().getBlockY(),
                            center.getLocation().getBlockZ(),
                            calculateEntityScore(entities.length),
                            entities.length,
                            nearest == null
                                    ? null
                                    : nearest.getName()
                    ));
                }

                if (tiles.length > 0) {

                    BlockState tile = tiles[0];

                    Player nearest = findNearestPlayer(
                            world,
                            tile.getX(),
                            tile.getY(),
                            tile.getZ(),
                            radius
                    );

                    hotspots.add(new LagHotspot(
                            HotspotType.TILE_ENTITY,
                            world.getName(),
                            tile.getX(),
                            tile.getY(),
                            tile.getZ(),
                            calculateTileScore(tiles.length),
                            tiles.length,
                            nearest == null
                                    ? null
                                    : nearest.getName()
                    ));
                }
            }
        }

        hotspots.sort(
                Comparator.comparingDouble(
                        LagHotspot::score
                ).reversed()
        );

        if (hotspots.size() > maxResults) {
            return List.copyOf(
                    hotspots.subList(0, maxResults)
            );
        }

        return List.copyOf(hotspots);
    }

    private Player findNearestPlayer(
            World world,
            double x,
            double y,
            double z,
            int radius
    ) {

        double radiusSquared =
                (double) radius * radius;

        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : world.getPlayers()) {

            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;

            double distance =
                    dx * dx +
                    dy * dy +
                    dz * dz;

            if (distance <= radiusSquared &&
                    distance < nearestDistance) {

                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    private double calculateEntityScore(int count) {
        return Math.min(100.0, count * 2.0);
    }

    private double calculateTileScore(int count) {
        return Math.min(100.0, count * 3.0);
    }
}
