package com.keyshard.api;

public record ErrorResponse(
        String code,
        String message,
        long timestamp
) {}