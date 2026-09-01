package com.kingbrezz.tickscope.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingbrezz.tickscope.TickScope;
import com.kingbrezz.tickscope.monitor.MetricsSnapshot;
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

        server.createContext(
                "/api/status",
                this::handleStatus
        );

        server.createContext(
                "/api/health",
                this::handleHealth
        );

        server.setExecutor(
                Executors.newCachedThreadPool()
        );

        server.start();

        plugin.getLogger().info(
                "Web dashboard API started at http://"
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
            send(
                    exchange,
                    401,
                    Map.of(
                            "error",
                            "Unauthorized"
                    )
            );
            return;
        }

        MetricsSnapshot snapshot =
                plugin.getPerformanceMonitor()
                        .collect();

        send(
                exchange,
                200,
                snapshot
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

        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
  }
