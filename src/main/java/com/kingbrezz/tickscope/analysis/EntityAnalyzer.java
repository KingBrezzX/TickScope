package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EntityAnalyzer {

    private final TickScope plugin;

    public EntityAnalyzer(TickScope plugin) {
        this.plugin = plugin;
    }

    public List<EntityHotspot> scan() {

        if (!plugin.getConfig().getBoolean(
                "analysis.entities", true)) {
            return List.of();
        }

        List<EntityHotspot> results = new ArrayList<>();

        for (World world : plugin.getServer().getWorlds()) {

            for (Chunk chunk : world.getLoadedChunks()) {

                Map<EntityType, Integer> counts = new HashMap<>();

                for (Entity entity : chunk.getEntities()) {

                    if (entity instanceof Player) {
                        continue;
                    }

                    counts.merge(
                            entity.getType(),
                            1,
                            Integer::sum
                    );
                }

                for (Map.Entry<EntityType, Integer> entry :
                        counts.entrySet()) {

                    int count = entry.getValue();

                    Entity representative = null;

                    for (Entity entity : chunk.getEntities()) {
                        if (entity.getType() == entry.getKey()) {
                            representative = entity;
                            break;
                        }
                    }

                    if (representative == null) {
                        continue;
                    }

                    Player nearest = findNearestPlayer(
                            world,
                            representative.getX(),
                            representative.getY(),
                            representative.getZ()
                    );

                    double score = Math.min(
                            100.0,
                            count * 2.0
                    );

                    results.add(new EntityHotspot(
                            world.getName(),
                            representative.getLocation().getBlockX(),
                            representative.getLocation().getBlockY(),
                            representative.getLocation().getBlockZ(),
                            entry.getKey().name(),
                            count,
                            score,
                            nearest == null
                                    ? null
                                    : nearest.getName()
                    ));
                }
            }
        }

        results.sort(
                Comparator.comparingDouble(
                        EntityHotspot::score
                ).reversed()
        );

        int limit = Math.max(
                1,
                plugin.getConfig().getInt(
                        "hotspots.max-results",
                        50
                )
        );

        if (results.size() > limit) {
            return List.copyOf(
                    results.subList(0, limit)
            );
        }

        return List.copyOf(results);
    }

    private Player findNearestPlayer(
            World world,
            double x,
            double y,
            double z
    ) {

        if (!plugin.getConfig().getBoolean(
                "hotspots.track-nearby-player",
                true)) {
            return null;
        }

        double bestDistance = Double.MAX_VALUE;
        Player nearest = null;

        for (Player player : world.getPlayers()) {

            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;

            double distance =
                    dx * dx +
                    dy * dy +
                    dz * dz;

            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }
                            }
