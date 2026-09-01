package com.kingbrezz.tickscope.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingbrezz.tickscope.TickScope;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RealtimeManager {

    private final TickScope plugin;

    private final Gson gson =
            new GsonBuilder().create();

    private final List<SseClient> clients =
            new CopyOnWriteArrayList<>();

    private int taskId = -1;

    public RealtimeManager(TickScope plugin) {
        this.plugin = plugin;
    }

    public void start() {

        if (!plugin.getConfig().getBoolean(
                "web.realtime.enabled",
                true
        )) {
            return;
        }

        long interval =
                Math.max(
                        1L,
                        plugin.getConfig().getLong(
                                "web.realtime.interval-ms",
                                1000L
                        ) / 50L
                );

        taskId =
                Bukkit.getScheduler().runTaskTimer(
                        plugin,
                        this::broadcast,
                        interval,
                        interval
                ).getTaskId();
    }

    private void broadcast() {

        if (clients.isEmpty()) {
            return;
        }

        String json =
                gson.toJson(
                        RealtimeSnapshot.create(
                                plugin
                        )
                );

        for (SseClient client : clients) {

            if (!client.send(json)) {
                clients.remove(client);
                client.close();
            }
        }
    }

    public void addClient(
            SseClient client
    ) {
        clients.add(client);
    }

    public void removeClient(
            SseClient client
    ) {

        clients.remove(client);
        client.close();
    }

    public void stop() {

        if (taskId != -1) {

            Bukkit.getScheduler()
                    .cancelTask(taskId);

            taskId = -1;
        }

        for (SseClient client : clients) {
            client.close();
        }

        clients.clear();
    }
}
