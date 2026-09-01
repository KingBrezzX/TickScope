package com.kingbrezz.tickscope.web;

public record WebServerInfo(
        String host,
        int port,
        boolean enabled
) {
}
