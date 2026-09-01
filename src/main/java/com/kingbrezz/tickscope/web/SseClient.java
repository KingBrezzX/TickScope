package com.kingbrezz.tickscope.web;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class SseClient {

    private final OutputStream output;

    public SseClient(OutputStream output) {
        this.output = output;
    }

    public synchronized boolean send(String data) {

        try {

            String message =
                    "data: "
                            + data
                            + "\n\n";

            output.write(
                    message.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            output.flush();

            return true;

        } catch (IOException exception) {

            return false;
        }
    }

    public void close() {

        try {
            output.close();
        } catch (IOException ignored) {
        }
    }
}
