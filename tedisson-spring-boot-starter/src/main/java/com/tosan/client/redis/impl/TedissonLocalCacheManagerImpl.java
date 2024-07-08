package com.tosan.client.redis.impl;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.SpringCacheConfig;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import com.tosan.client.redis.cacheconfig.LocalCacheConfig;
import com.tosan.client.redis.enumuration.LocalCacheProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 9/24/2022
 */
public class TedissonLocalCacheManagerImpl extends TedissonCacheManagerBase implements TedissonCacheManager {

    private final LocalCacheManager localCacheManager;

    public TedissonLocalCacheManagerImpl(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public boolean isKeyInCache(String cacheName, String key) {
        return localCacheManager.isKeyInCache(cacheName, key);
    }

    @Override
    public void createCache(String cacheName, CacheConfig cacheConfig) {
        localCacheManager.createCache(cacheName, getLocalCacheConfig(cacheConfig));
    }

    @Override
    public void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy) {
        localCacheManager.replaceCacheExpirationConfig(cacheName, newCacheExpiryPolicy);
    }

    @Override
    public <T> T getItemFromCache(String cacheName, String key) {
        return localCacheManager.getItemFromCache(cacheName, key);
    }

    @Override
    public void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        localCacheManager.addItemToCache(cacheName, key, value, timeToLive, timeToIdle, timeUnit);
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items) {
        localCacheManager.addAllToCache(cacheName, items);
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        localCacheManager.addAllToCache(cacheName, items, timeToLive, timeUnit);
    }

    @Override
    public void replaceCacheItem(String cacheName, String key, Object value) {
        localCacheManager.replaceCacheItem(cacheName, key, value);
    }

    @Override
    public void removeItemFromCache(String cacheName, String key) {
        localCacheManager.removeItemFromCache(cacheName, key);
    }

    @Override
    public void clearCache(String cacheName) {
        localCacheManager.clearCache(cacheName);
    }

    @Override
    public boolean isCacheEmpty(String cacheName) {
        return localCacheManager.isCacheEmpty(cacheName);
    }

    @Override
    public long getCacheSize(String cacheName) {
        return localCacheManager.getCacheSize(cacheName);
    }

    @Override
    public Set<String> getCacheKeySet(String cacheName) {
        return localCacheManager.getCacheKeySet(cacheName);
    }

    @Override
    public CacheExpiryPolicy getCacheExpirationConfig(String cacheName) {
        return localCacheManager.getCacheExpirationConfig(cacheName);
    }

    @Override
    public void initializeAtomicLongCache(String cacheName, String key, CacheExpiryPolicy cacheExpiryPolicy) {
        resetAtomicItem(cacheName, key);
    }

    @Override
    public long incrementAndGetAtomicItem(String cacheName, String key) {
        return localCacheManager.incrementAndGetAtomicItem(cacheName, key);
    }

    @Override
    public void resetAtomicItem(String cacheName, String key) {
        localCacheManager.resetAtomicItem(cacheName, key);
    }

    @Override
    public long getAtomicValue(String cacheName, String key) {
        return localCacheManager.getAtomicValue(cacheName, key);
    }

    @Override
    public void setAtomicItem(String cacheName, String key, long value) {
        localCacheManager.setAtomicItem(cacheName, key, value);
    }

    @Override
    public void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit) {
        localCacheManager.expireAtomicItem(cacheName, key, timeToLive, timeUnit);
    }

    @Override
    public void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        localCacheManager.updateItemExpiration(cacheName, key, timeToLive, timeToIdle, timeUnit);
    }

    @Override
    public Boolean isRedisEnabled() {
        return false;
    }

    @Override
    public LocalCacheProvider getLocalCacheProvider() {
        return localCacheManager.getCacheProvider();
    }

    @Override
    public CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs) {
        return localCacheManager.getSpringCacheManager(cacheConfigs);
    }

    private LocalCacheConfig getLocalCacheConfig(CacheConfig cacheConfig) {
        if (cacheConfig == null) {
            return null;
        }
        LocalCacheConfig localCacheConfig = new LocalCacheConfig();
        localCacheConfig.setMaxSize(cacheConfig.getMaxSize());
        if (CollectionUtils.isNotEmpty(cacheConfig.getListeners())) {
            localCacheConfig.setCacheListener(cacheConfig.getListeners().get(0));
        }
        localCacheConfig.setExpiryPolicy(cacheConfig.getExpiryPolicy());
        return localCacheConfig;
    }
}
