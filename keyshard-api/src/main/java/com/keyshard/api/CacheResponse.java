package com.keyshard.api;

public record CacheResponse(
        String key,
        String value
) {}