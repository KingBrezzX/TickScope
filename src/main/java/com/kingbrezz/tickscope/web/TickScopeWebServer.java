package com.kingbrezz.tickscope.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingbrezz.tickscope.TickScope;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;

public final class TickScopeWebServer {

    private final TickScope plugin;
    private final Gson gson =
            new GsonBuilder().create();

    private HttpServer server;
    private RealtimeManager realtimeManager;

    public TickScopeWebServer(TickScope plugin) {
        this.plugin = plugin;
    }

    public void start() throws IOException {

        String host =
                plugin.getConfig().getString(
                        "web.host",
                        "127.0.0.1"
                );

        int port =
                plugin.getConfig().getInt(
                        "web.port",
                        8765
                );

        server = HttpServer.create(
                new InetSocketAddress(
                        host,
                        port
                ),
                0
        );

        realtimeManager =
                new RealtimeManager(plugin);

        server.createContext(
                "/api/health",
                this::handleHealth
        );

        server.createContext(
                "/api/status",
                this::handleStatus
        );

        server.createContext(
                "/api/server",
                this::handleServer
        );

        server.createContext(
                "/api/uptime",
                this::handleUptime
        );

        server.createContext(
                "/api/spikes",
                this::handleSpikes
        );

        server.createContext(
                "/api/hotspots",
                this::handleHotspots
        );

        server.createContext(
                "/api/redstone",
                this::handleRedstone
        );

        server.createContext(
                "/api/entities",
                this::handleEntities
        );

        server.createContext(
                "/api/tile-entities",
                this::handleTileEntities
        );

        server.createContext(
                "/api/recommendations",
                this::handleRecommendations
        );

        server.createContext(
                "/api/stream",
                this::handleStream
        );

        server.setExecutor(
                Executors.newCachedThreadPool()
        );

        server.start();

        realtimeManager.start();

        plugin.getLogger().info(
                "TickScope web API started on "
                        + host
                        + ":"
                        + port
        );
    }

    private void handleHealth(
            HttpExchange exchange
    ) throws IOException {

        send(
                exchange,
                200,
                Map.of(
                        "status",
                        "ok",
                        "plugin",
                        "TickScope"
                )
        );
    }

    private void handleStatus(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.status(plugin)
        );
    }

    private void handleServer(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiUtil.serverInfo(plugin)
        );
    }

    private void handleUptime(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                UptimeApi.get(plugin)
        );
    }

    private void handleSpikes(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                SpikeApi.get(plugin)
        );
    }

    private void handleHotspots(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.hotspots(plugin)
        );
    }

    private void handleRedstone(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.redstone(plugin)
        );
    }

    private void handleEntities(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.entities(plugin)
        );
    }

    private void handleTileEntities(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.tileEntities(plugin)
        );
    }

    private void handleRecommendations(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        send(
                exchange,
                200,
                ApiData.recommendations(plugin)
        );
    }

    private void handleStream(
            HttpExchange exchange
    ) throws IOException {

        if (!authorized(exchange)) {
            unauthorized(exchange);
            return;
        }

        Headers headers =
                exchange.getResponseHeaders();

        headers.set(
                "Content-Type",
                "text/event-stream"
        );

        headers.set(
                "Cache-Control",
                "no-cache"
        );

        headers.set(
                "Connection",
                "keep-alive"
        );

        headers.set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.sendResponseHeaders(
                200,
                0
        );

        SseClient client =
                new SseClient(
                        exchange.getResponseBody()
                );

        realtimeManager.addClient(client);

        client.send(
                gson.toJson(
                        RealtimeSnapshot.create(
                                plugin
                        )
                )
        );
    }

    private boolean authorized(
            HttpExchange exchange
    ) {

        if (!plugin.getConfig().getBoolean(
                "web.authentication.enabled",
                true
        )) {
            return true;
        }

        String supplied =
                exchange.getRequestHeaders()
                        .getFirst("Authorization");

        if (supplied == null) {
            return false;
        }

        String expected =
                "Bearer "
                        + plugin.getTokenManager()
                        .getToken();

        return supplied.equals(expected);
    }

    private void unauthorized(
            HttpExchange exchange
    ) throws IOException {

        send(
                exchange,
                401,
                Map.of(
                        "error",
                        "Unauthorized"
                )
        );
    }

    private void send(
            HttpExchange exchange,
            int status,
            Object object
    ) throws IOException {

        byte[] data =
                gson.toJson(object)
                        .getBytes(
                                StandardCharsets.UTF_8
                        );

        exchange.getResponseHeaders()
                .set(
                        "Content-Type",
                        "application/json; charset=utf-8"
                );

        exchange.getResponseHeaders()
                .set(
                        "Access-Control-Allow-Origin",
                        "*"
                );

        exchange.sendResponseHeaders(
                status,
                data.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(data);
        }
    }

    public void stop() {

        if (realtimeManager != null) {
            realtimeManager.stop();
            realtimeManager = null;
        }

        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
                    }
