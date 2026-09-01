package com.kingbrezz.tickscope.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kingbrezz.tickscope.TickScope;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class TickScopeWebServer {
    private final TickScope plugin;
    private final Gson gson = new GsonBuilder().create();
    private HttpServer server;
    private RealtimeManager realtimeManager;

    public TickScopeWebServer(TickScope plugin) {
        this.plugin = plugin;
    }

    public void start() throws IOException {
        String host = plugin.getConfig().getString("web.host", "127.0.0.1");
        int port = plugin.getConfig().getInt("web.port", 19132);

        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        realtimeManager = new RealtimeManager(plugin);

        server.createContext("/", this::route);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        realtimeManager.start();

        plugin.getLogger().info("TickScope API listening on http://" + host + ":" + port);
        plugin.getLogger().info("TickScope API endpoint: /api/v1/all");
    }

    private void route(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();

        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            cors(ex);
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())
                && !path.equals("/api/admin/destroy")
                && !path.equals("/api/admin/ban")
                && !path.equals("/api/v1/admin/destroy")
                && !path.equals("/api/v1/admin/ban")) {
            send(ex, 405, Map.of("success", false, "error", "Method not allowed"));
            return;
        }

        // Public health endpoints.
        if (path.equals("/") || path.equals("/health")
                || path.equals("/api/health") || path.equals("/api/v1/health")) {
            send(ex, 200, health());
            return;
        }

        // Normalize only after health handling so /api/v1 paths remain explicit.
        String apiPath = path.startsWith("/api/v1/")
                ? "/api/" + path.substring("/api/v1/".length())
                : path;

        // Static web resources.
        if (apiPath.equals("/") || apiPath.equals("/index.html") || apiPath.equals("/connect.html")
                || apiPath.endsWith(".js") || apiPath.endsWith(".css")
                || apiPath.endsWith(".ico") || apiPath.endsWith(".json")) {
            serveStatic(ex, apiPath);
            return;
        }

        if (!authorized(ex)) {
            send(ex, 401, Map.of(
                    "success", false,
                    "error", "Unauthorized"
            ));
            return;
        }

        switch (apiPath) {
            case "/api/all", "/api/status" -> send(ex, 200, ApiData.all(plugin));
            case "/api/mspt" -> send(ex, 200, ApiData.status(plugin));
            case "/api/health" -> send(ex, 200, health());
            case "/api/server" -> send(ex, 200, ApiUtil.serverInfo(plugin));
            case "/api/uptime" -> send(ex, 200, UptimeApi.get(plugin));
            case "/api/spikes" -> send(ex, 200, SpikeApi.get(plugin));
            case "/api/hotspots" -> send(ex, 200, ApiData.hotspots(plugin));
            case "/api/redstone" -> send(ex, 200, ApiData.redstone(plugin));
            case "/api/entities" -> send(ex, 200, ApiData.entities(plugin));
            case "/api/tile-entities" -> send(ex, 200, ApiData.tileEntities(plugin));
            case "/api/recommendations" -> send(ex, 200, ApiData.recommendations(plugin));
            case "/api/admin/destroy" -> adminDestroy(ex);
            case "/api/admin/ban" -> adminBan(ex);
            case "/api/stream" -> stream(ex);
            default -> send(ex, 404, Map.of(
                    "success", false,
                    "error", "Endpoint not found",
                    "path", path
            ));
        }
    }

    private Map<String, Object> health() {
        return Map.of(
                "success", true,
                "status", "ok",
                "plugin", "TickScope",
                "version", plugin.getDescription().getVersion(),
                "server", plugin.getServer().getName(),
                "endpoint", "/api/v1/all"
        );
    }

    private void adminDestroy(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, Map.of("success", false, "error", "Method not allowed"));
            return;
        }

        JsonObject body = readJson(ex);
        String world = body.has("world") ? body.get("world").getAsString() : null;
        String player = body.has("player") && !body.get("player").isJsonNull()
                ? body.get("player").getAsString() : null;

        if (world == null || !body.has("x") || !body.has("y") || !body.has("z")) {
            send(ex, 400, Map.of("success", false, "error", "world, x, y and z are required"));
            return;
        }

        boolean autoBan = body.has("autoBan") && body.get("autoBan").getAsBoolean();

        try {
            send(ex, 200, plugin.getServer().getScheduler().callSyncMethod(plugin, () ->
                    AdminActionApi.destroy(plugin, world,
                            body.get("x").getAsInt(),
                            body.get("y").getAsInt(),
                            body.get("z").getAsInt(),
                            player, autoBan)
            ).get(30, TimeUnit.SECONDS));
        } catch (IllegalArgumentException e) {
            send(ex, 400, Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            plugin.getLogger().warning("Admin destroy failed: " + e.getMessage());
            send(ex, 500, Map.of("success", false, "error", "Admin action failed"));
        }
    }

    private void adminBan(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, Map.of("success", false, "error", "Method not allowed"));
            return;
        }

        JsonObject body = readJson(ex);
        String player = body.has("player")
                ? body.get("player").getAsString().trim() : "";

        if (player.isBlank()) {
            send(ex, 400, Map.of("success", false, "error", "player is required"));
            return;
        }

        try {
            send(ex, 200, plugin.getServer().getScheduler().callSyncMethod(plugin, () ->
                    AdminActionApi.ban(plugin, player)
            ).get(30, TimeUnit.SECONDS));
        } catch (IllegalArgumentException e) {
            send(ex, 400, Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            plugin.getLogger().warning("Admin ban failed: " + e.getMessage());
            send(ex, 500, Map.of("success", false, "error", "Admin action failed"));
        }
    }

    private JsonObject readJson(HttpExchange ex) throws IOException {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");

        if (contentType == null
                || !contentType.toLowerCase(java.util.Locale.ROOT)
                .startsWith("application/json")) {
            throw new IllegalArgumentException("Content-Type must be application/json");
        }

        byte[] raw = ex.getRequestBody().readAllBytes();

        if (raw.length == 0 || raw.length > 32_768) {
            throw new IOException("Invalid request body size");
        }

        JsonObject json = gson.fromJson(
                new String(raw, StandardCharsets.UTF_8),
                JsonObject.class
        );

        if (json == null) {
            throw new IllegalArgumentException("Invalid JSON body");
        }

        return json;
    }

    private void stream(HttpExchange ex) throws IOException {
        if (!authorized(ex)) {
            send(ex, 401, Map.of("success", false, "error", "Unauthorized"));
            return;
        }

        cors(ex);
        ex.getResponseHeaders().set(
                "Content-Type",
                "text/event-stream; charset=utf-8"
        );
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.getResponseHeaders().set("Connection", "keep-alive");

        ex.sendResponseHeaders(200, 0);

        SseClient client = new SseClient(ex.getResponseBody());
        realtimeManager.addClient(client);
        client.send(gson.toJson(RealtimeSnapshot.create(plugin)));
    }

    private boolean authorized(HttpExchange ex) {
        if (!plugin.getConfig()
                .getBoolean("web.authentication.enabled", true)) {
            return true;
        }

        String supplied =
                ex.getRequestHeaders().getFirst("Authorization");

        if (supplied == null) {
            String token = queryToken(ex.getRequestURI());
            supplied = token == null ? null : "Bearer " + token;
        }

        return supplied != null
                && supplied.equals(
                "Bearer " + plugin.getTokenManager().getToken()
        );
    }

    private String queryToken(URI uri) {
        String q = uri.getRawQuery();

        if (q == null) {
            return null;
        }

        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);

            if (kv.length == 2 && kv[0].equals("token")) {
                return URLDecoder.decode(
                        kv[1],
                        StandardCharsets.UTF_8
                );
            }
        }

        return null;
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        if (path.contains("..") || path.contains("\\")) {
            send(ex, 400, Map.of(
                    "success", false,
                    "error", "Invalid path"
            ));
            return;
        }

        String resource =
                path.equals("/") ? "/web/index.html" : "/web" + path;

        try (InputStream in =
                     TickScopeWebServer.class.getResourceAsStream(resource)) {

            if (in == null) {
                send(ex, 404, Map.of(
                        "success", false,
                        "error", "Web resource not found"
                ));
                return;
            }

            byte[] data = in.readAllBytes();

            String type =
                    path.endsWith(".html") || path.equals("/")
                            ? "text/html; charset=utf-8"
                            : path.endsWith(".js")
                            ? "application/javascript; charset=utf-8"
                            : "text/css";

            cors(ex);
            ex.getResponseHeaders().set("Content-Type", type);
            ex.sendResponseHeaders(200, data.length);

            try (OutputStream out = ex.getResponseBody()) {
                out.write(data);
            }
        }
    }

    private void cors(HttpExchange ex) {
        if (!plugin.getConfig()
                .getBoolean("web.cors.enabled", true)) {
            return;
        }

        String requestOrigin =
                ex.getRequestHeaders().getFirst("Origin");

        java.util.List<String> configuredOrigins =
                plugin.getConfig()
                        .getStringList("web.cors.allowed-origins");

        String allow =
                configuredOrigins.isEmpty()
                        || configuredOrigins.contains("*")
                        ? "*"
                        : "";

        if (requestOrigin != null
                && !configuredOrigins.isEmpty()
                && !configuredOrigins.contains("*")) {

            for (String origin : configuredOrigins) {
                if (origin.trim().equals(requestOrigin)) {
                    allow = requestOrigin;
                    break;
                }
            }
        }

        if (allow.isEmpty()) {
            allow = configuredOrigins.isEmpty()
                    ? "*"
                    : configuredOrigins.get(0);
        }

        ex.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                allow
        );

        ex.getResponseHeaders().set("Vary", "Origin");

        ex.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept, Cache-Control"
        );

        ex.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, OPTIONS"
        );

        ex.getResponseHeaders().set(
                "Access-Control-Max-Age",
                "600"
        );
    }

    private void send(
            HttpExchange ex,
            int status,
            Object value
    ) throws IOException {

        byte[] data =
                gson.toJson(value)
                        .getBytes(StandardCharsets.UTF_8);

        cors(ex);

        ex.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=utf-8"
        );

        ex.sendResponseHeaders(status, data.length);

        try (OutputStream out = ex.getResponseBody()) {
            out.write(data);
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
