package com.keyshard.api;

public record CacheKeyHash(
        String key,
        int hash
) {}