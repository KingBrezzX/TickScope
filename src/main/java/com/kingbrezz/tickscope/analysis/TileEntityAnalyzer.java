package com.kingbrezz.tickscope.analysis;

import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TileEntityAnalyzer {

    private final TickScope plugin;

    public TileEntityAnalyzer(TickScope plugin) {
        this.plugin = plugin;
    }

    public List<LagHotspot> scan() {

        if (!plugin.getConfig().getBoolean(
                "analysis.tile-entities",
                true)) {
            return List.of();
        }

        List<LagHotspot> results = new ArrayList<>();

        for (World world : plugin.getServer().getWorlds()) {

            Chunk[] loadedChunks = world.getLoadedChunks();
            int maxChunks = plugin.getConfig().getInt("hotspots.max-loaded-chunks", 1000);
            int scanCount = maxChunks <= 0 ? loadedChunks.length : Math.min(maxChunks, loadedChunks.length);
            for (int chunkIndex = 0; chunkIndex < scanCount; chunkIndex++) {
                Chunk chunk = loadedChunks[chunkIndex];

                BlockState[] states =
                        chunk.getTileEntities();

                if (states.length == 0) {
                    continue;
                }

                for (BlockState state : states) {

                    double score = Math.min(
                            100.0,
                            states.length * 3.0
                    );

                    results.add(new LagHotspot(
                            HotspotType.TILE_ENTITY,
                            world.getName(),
                            state.getX(),
                            state.getY(),
                            state.getZ(),
                            score,
                            states.length,
                            null
                    ));
                }
            }
        }

        results.sort(
                Comparator.comparingDouble(
                        LagHotspot::score
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
            }
