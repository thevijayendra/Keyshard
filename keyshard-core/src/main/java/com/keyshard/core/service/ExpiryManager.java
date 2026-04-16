package com.keyshard.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@EnableScheduling
@Component
public class ExpiryManager {
    @Autowired
    private CacheService<?, ?> cacheService;

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    public void cleanExpiredKeys() {
        // Logic to iterate and remove keys where System.currentTimeMillis() > expiry
    }
}