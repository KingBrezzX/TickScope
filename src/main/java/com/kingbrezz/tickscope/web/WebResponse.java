package com.kingbrezz.tickscope.web;

public record WebResponse(
        int status,
        String contentType,
        String body
) {
}
