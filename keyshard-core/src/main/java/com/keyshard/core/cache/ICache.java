package com.keyshard.core.cache;

import com.keyshard.core.model.CacheEntry;
import com.keyshard.core.exception.KeyNotFoundException;

import java.util.Map;

public interface ICache<K, V> {
    
    void put(K key, V value, long ttlMillis);
    
    V get(K key) throws KeyNotFoundException;
    
    void delete(K key);
    
    // Logic execution for registered domains
    default <R> R execute(K key, EntryProcessor<V, R> processor) {
        throw new UnsupportedOperationException("Entry processing not implemented");
    }

    Map<K, CacheEntry<V>> getInternalStore();
}