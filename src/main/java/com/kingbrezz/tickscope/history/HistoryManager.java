package com.kingbrezz.tickscope.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kingbrezz.tickscope.TickScope;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class HistoryManager {

    private static final long DAY =
            24L * 60L * 60L * 1000L;

    private final TickScope plugin;
    private final Gson gson =
            new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    private final List<HistoryEntry> entries =
            new ArrayList<>();

    public HistoryManager(TickScope plugin) {
        this.plugin = plugin;

        file = plugin.getDataFolder()
                .toPath()
                .resolve("history.json");
    }

    public void load() {

        try {

            Files.createDirectories(
                    plugin.getDataFolder().toPath()
            );

            if (!Files.exists(file)) {
                return;
            }

            try (Reader reader =
                         Files.newBufferedReader(file)) {

                List<HistoryEntry> loaded =
                        gson.fromJson(
                                reader,
                                new TypeToken<List<HistoryEntry>>() {
                                }.getType()
                        );

                if (loaded != null) {
                    entries.clear();
                    entries.addAll(loaded);
                }
            }

            cleanup();

        } catch (IOException exception) {

            plugin.getLogger().warning(
                    "Unable to load history: "
                            + exception.getMessage()
            );
        }
    }

    public synchronized void add(
            HistoryEntry entry
    ) {

        if (!plugin.getConfig().getBoolean(
                "history.enabled",
                true
        )) {
            return;
        }

        entries.add(entry);

        save();
    }

    public synchronized List<HistoryEntry> getEntries() {
        return List.copyOf(entries);
    }

    public synchronized void cleanup() {

        long retentionDays =
                Math.max(
                        1,
                        plugin.getConfig().getLong(
                                "history.retention-days",
                                7
                        )
                );

        long cutoff =
                System.currentTimeMillis()
                        - retentionDays * DAY;

        entries.removeIf(
                entry -> entry.timestamp() < cutoff
        );

        save();
    }

    public synchronized void save() {

        try {

            Files.createDirectories(
                    plugin.getDataFolder().toPath()
            );

            try (Writer writer =
                         Files.newBufferedWriter(file)) {

                gson.toJson(entries, writer);
            }

        } catch (IOException exception) {

            plugin.getLogger().warning(
                    "Unable to save history: "
                            + exception.getMessage()
            );
        }
    }
          }
