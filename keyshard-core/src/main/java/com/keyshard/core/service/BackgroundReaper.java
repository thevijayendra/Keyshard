package com.keyshard.core.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackgroundReaper {

    // Use wildcards here
    private final CacheService<?, ?> cacheService;

    public BackgroundReaper(CacheService<?, ?> cacheService) {
        this.cacheService = cacheService;
    }

    @Scheduled(fixedDelay = 10000)
    public void reap() {
        // Use 'var' (Java 11+) or explicitly define the wildcard map
        var store = cacheService.getInternalStore(); 
        
        // Or explicitly:
        // Map<?, CacheEntry<?>> store = cacheService.getInternalStore();

        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}