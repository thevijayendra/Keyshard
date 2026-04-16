package com.keyshard.core.stats;

public record CacheStats(
        long hits,
        long misses,
        long puts,
        long evictions
) {}