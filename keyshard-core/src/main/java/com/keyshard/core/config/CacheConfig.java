package com.keyshard.core.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//@Component
//@ConfigurationPropertiesScan("com.keyshard.core")
@Getter
@Setter
@Component
//@EnableConfigurationProperties(CacheConfig.class)
@ConfigurationProperties(prefix = "dkvs")
@Data
public class CacheConfig {

    private int maxEntries = 1000;      // Default value

    private String evictionPolicy = "LRU"; // Default value
    // ...
}