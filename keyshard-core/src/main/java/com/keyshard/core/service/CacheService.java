package com.keyshard.core.service;

import com.keyshard.core.model.CacheEntry;
import com.keyshard.core.cache.ICache;
import com.keyshard.core.exception.KeyNotFoundException;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService<K, V> implements ICache<K, V> {

    private final int MAX_CAPACITY = 1000;
    private final Map<K, CacheEntry<V>> store = new ConcurrentHashMap<>();

    private final Map<K, Boolean> lruOrder = Collections.synchronizedMap(
        new LinkedHashMap<K, Boolean>(MAX_CAPACITY, 0.75f, true)
    );

    @Override
    public void put(K key, V value, long ttlMillis) {
        if (store.size() >= MAX_CAPACITY && !store.containsKey(key)) {
            evictOldest();
        }
        store.put(key, new CacheEntry<>(value, ttlMillis));
        lruOrder.put(key, true);
    }

    @Override
    public V get(K key) {
        CacheEntry<V> entry = store.get(key);

        if (entry == null) {
            throw new KeyNotFoundException(key.toString());
        }

        if (entry.isExpired()) {
            delete(key);
            throw new KeyNotFoundException(key.toString());
        }

        lruOrder.get(key); 
        return entry.getValue();
    }

    @Override
    public void delete(K key) {
        store.remove(key);
        lruOrder.remove(key);
    }

    private void evictOldest() {
        K eldestKey = null;
        synchronized (lruOrder) {
            Iterator<K> it = lruOrder.keySet().iterator();
            if (it.hasNext()) {
                eldestKey = it.next();
            }
        }
        if (eldestKey != null) {
            delete(eldestKey);
        }
    }

    @Override
    public Map<K, CacheEntry<V>> getInternalStore() {
        return this.store;
    }
}