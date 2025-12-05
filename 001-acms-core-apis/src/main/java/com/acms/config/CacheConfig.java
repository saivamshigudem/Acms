package com.acms.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    
    public static final String AGENT_CACHE = "agents";
    public static final String POLICY_CACHE = "policies";
    public static final String COMMISSION_CACHE = "commissions";
    public static final String PAYMENT_CACHE = "payments";
    
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            AGENT_CACHE, POLICY_CACHE, COMMISSION_CACHE, PAYMENT_CACHE);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
