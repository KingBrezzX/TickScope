package com.kingbrezz.tickscope.web;

import com.kingbrezz.tickscope.TickScope;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public final class TokenManager {

    private final TickScope plugin;
    private final Path tokenFile;

    private String token;

    public TokenManager(TickScope plugin) {
        this.plugin = plugin;

        tokenFile = plugin.getDataFolder()
                .toPath()
                .resolve("server.token");
    }

    public void load() {

        try {
            Files.createDirectories(
                    plugin.getDataFolder().toPath()
            );

            if (Files.exists(tokenFile)) {

                token = Files.readString(
                        tokenFile
                ).trim();

                if (!token.isBlank()) {
                    return;
                }
            }

            generate();

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Unable to load TickScope authentication token",
                    exception
            );
        }
    }

    private void generate() {

        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        StringBuilder builder =
                new StringBuilder();

        for (byte value : bytes) {
            builder.append(
                    String.format(
                            "%02x",
                            value
                    )
            );
        }

        token = "TS_" + builder;

        try {
            Files.writeString(
                    tokenFile,
                    token
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to save TickScope token",
                    exception
            );
        }
    }

    public String getToken() {
        return token;
    }
  }
