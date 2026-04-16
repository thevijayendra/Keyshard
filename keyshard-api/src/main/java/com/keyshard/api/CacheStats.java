package com.keyshard.api;

public record CacheStats(
        long hits,
        long misses,
        long puts,
        long evictions
) {}