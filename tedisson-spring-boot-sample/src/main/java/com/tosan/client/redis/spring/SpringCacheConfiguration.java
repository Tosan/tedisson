package com.tosan.client.redis.spring;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.SpringCacheConfig;
import com.tosan.client.redis.api.TedissonCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

/**
 * @author R.Mehri
 * @since 8/14/2023
 */
@EnableCaching
@Configuration
public class SpringCacheConfiguration {

    @Autowired
    private LocalCacheManager localCacheManager;

    @Autowired
    private TedissonCacheManager cacheManager;

    @Bean("localSpringCacheManager")
    @Primary
    public CacheManager localSpringCacheManager() {
        List<SpringCacheConfig> cacheConfigList = new ArrayList<>();
        SpringCacheConfig cacheConfig = new SpringCacheConfig();
        cacheConfig.setCacheName("addresses");
        cacheConfig.setTimeToLive(5);
        cacheConfig.setMaxSize(10);
        cacheConfigList.add(cacheConfig);
        return localCacheManager.getSpringCacheManager(cacheConfigList);
    }

    @Bean("tedissonSpringCacheManager")
    public CacheManager tedissonSpringCacheManager() {
        List<SpringCacheConfig> cacheConfigList = new ArrayList<>();
        SpringCacheConfig cacheConfig = new SpringCacheConfig();
        cacheConfig.setCacheName("addresses1");
        cacheConfig.setTimeToLive(5);
        cacheConfig.setMaxSize(10);
        cacheConfigList.add(cacheConfig);
        return cacheManager.getSpringCacheManager(cacheConfigList);
    }
}
