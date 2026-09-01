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
import java.util.Map;
import java.util.concurrent.Executors;

public final class TickScopeWebServer {
    private final TickScope plugin;
    private final Gson gson = new GsonBuilder().create();
    private HttpServer server;
    private RealtimeManager realtimeManager;

    public TickScopeWebServer(TickScope plugin) { this.plugin = plugin; }

    public void start() throws IOException {
        String host = plugin.getConfig().getString("web.host", "127.0.0.1");
        int port = plugin.getConfig().getInt("web.port", 8765);
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        realtimeManager = new RealtimeManager(plugin);

        server.createContext("/", this::route);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        realtimeManager.start();
        plugin.getLogger().info("Dashboard: http://" + host + ":" + port + "/");
    }

    private void route(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            cors(ex);
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return;
        }

        if (path.equals("/") || path.equals("/index.html") || path.equals("/connect.html")
                || path.endsWith(".js") || path.endsWith(".css")) {
            serveStatic(ex, path);
            return;
        }

        if (!authorized(ex) && !path.equals("/api/health")) {
            send(ex, 401, Map.of("error", "Unauthorized"));
            return;
        }

        switch (path) {
            case "/api/health" -> send(ex, 200, Map.of("status", "ok", "plugin", "TickScope"));
            case "/api/status" -> send(ex, 200, ApiData.status(plugin));
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
            default -> send(ex, 404, Map.of("error", "Endpoint not found"));
        }
    }

    private void adminDestroy(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, Map.of("error", "Method not allowed"));
            return;
        }
        JsonObject body = readJson(ex);
        String world = body.has("world") ? body.get("world").getAsString() : null;
        String player = body.has("player") && !body.get("player").isJsonNull()
                ? body.get("player").getAsString() : null;
        if (world == null || !body.has("x") || !body.has("y") || !body.has("z")) {
            send(ex, 400, Map.of("error", "world, x, y and z are required"));
            return;
        }
        boolean autoBan = body.has("autoBan") && body.get("autoBan").getAsBoolean();
        try {
            send(ex, 200, AdminActionApi.destroy(plugin, world, body.get("x").getAsInt(),
                    body.get("y").getAsInt(), body.get("z").getAsInt(), player, autoBan));
        } catch (IllegalArgumentException e) {
            send(ex, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            plugin.getLogger().warning("Admin destroy failed: " + e.getMessage());
            send(ex, 500, Map.of("error", "Admin action failed"));
        }
    }

    private void adminBan(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, Map.of("error", "Method not allowed"));
            return;
        }
        JsonObject body = readJson(ex);
        String player = body.has("player") ? body.get("player").getAsString().trim() : "";
        if (player.isBlank()) {
            send(ex, 400, Map.of("error", "player is required"));
            return;
        }
        send(ex, 200, AdminActionApi.ban(plugin, player));
    }

    private JsonObject readJson(HttpExchange ex) throws IOException {
        byte[] raw = ex.getRequestBody().readAllBytes();
        if (raw.length > 32_768) throw new IOException("Request too large");
        return gson.fromJson(new String(raw, StandardCharsets.UTF_8), JsonObject.class);
    }

    private void stream(HttpExchange ex) throws IOException {
        if (!authorized(ex)) { send(ex, 401, Map.of("error", "Unauthorized")); return; }
        cors(ex);
        ex.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.getResponseHeaders().set("Connection", "keep-alive");
        ex.sendResponseHeaders(200, 0);
        SseClient client = new SseClient(ex.getResponseBody());
        realtimeManager.addClient(client);
        client.send(gson.toJson(RealtimeSnapshot.create(plugin)));
    }

    private boolean authorized(HttpExchange ex) {
        if (!plugin.getConfig().getBoolean("web.authentication.enabled", true)) return true;
        String supplied = ex.getRequestHeaders().getFirst("Authorization");
        if (supplied == null) {
            String token = queryToken(ex.getRequestURI());
            supplied = token == null ? null : "Bearer " + token;
        }
        return supplied != null && supplied.equals("Bearer " + plugin.getTokenManager().getToken());
    }

    private String queryToken(URI uri) {
        String q = uri.getRawQuery();
        if (q == null) return null;
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals("token"))
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
        }
        return null;
    }

    private void serveStatic(HttpExchange ex, String path) throws IOException {
        String resource = path.equals("/") ? "/web/index.html" : "/web" + path;
        try (InputStream in = TickScopeWebServer.class.getResourceAsStream(resource)) {
            if (in == null) { send(ex, 404, Map.of("error", "Web resource not found")); return; }
            byte[] data = in.readAllBytes();
            String type = path.endsWith(".html") || path.equals("/") ? "text/html; charset=utf-8"
                    : path.endsWith(".js") ? "application/javascript; charset=utf-8"
                    : "text/css; charset=utf-8";
            cors(ex);
            ex.getResponseHeaders().set("Content-Type", type);
            ex.sendResponseHeaders(200, data.length);
            try (OutputStream out = ex.getResponseBody()) { out.write(data); }
        }
    }

    private void cors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Authorization, Content-Type");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private void send(HttpExchange ex, int status, Object value) throws IOException {
        byte[] data = gson.toJson(value).getBytes(StandardCharsets.UTF_8);
        cors(ex);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(data); }
    }

    public void stop() {
        if (realtimeManager != null) { realtimeManager.stop(); realtimeManager = null; }
        if (server != null) { server.stop(0); server = null; }
    }
}
