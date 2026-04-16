package com.keyshard.api;

public record CacheRequest(
        String value,
        long ttl // milliseconds
) {}