package com.keyshard.core.model;

import lombok.Getter;
import java.time.Instant;

/**
 * Wrapper for the cached value including metadata for expiration and eviction.
 */
@Getter
public class CacheEntry<V> {
    private final V value;
    private final long expiryTimestamp; // Epoch milliseconds
    private final long createdAt;
    
    // Volatile ensures visibility across threads if you update access time
    private volatile long lastAccessedAt; 

    public CacheEntry(V value, long ttlMillis) {
        this.value = value;
        this.createdAt = System.currentTimeMillis();
        this.expiryTimestamp = this.createdAt + ttlMillis;
        this.lastAccessedAt = this.createdAt;
    }

    /**
     * Checks if the entry has lived past its TTL.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTimestamp;
    }

    /**
     * Updates the last access timestamp for LRU tracking.
     */
    public void recordAccess() {
        this.lastAccessedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return String.format("CacheEntry(value=%s, expiresAt=%s)", 
            value, Instant.ofEpochMilli(expiryTimestamp));
    }
}