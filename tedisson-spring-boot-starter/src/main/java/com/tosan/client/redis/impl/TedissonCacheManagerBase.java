package com.tosan.client.redis.impl;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import com.tosan.client.redis.cacheconfig.CentralCacheTypeConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 06/01/2021
 */
@Slf4j
public abstract class TedissonCacheManagerBase {

    private static final String CACHE_PREFIX = "REDIS_";
    public static final String CACHE_EXPIRY_CONFIG_NAME = CACHE_PREFIX + "CACHE_EXPIRY_CONFIG_NAME";
    public final String instanceID = UUID.randomUUID().toString();
    protected Map<String, CentralCacheTypeConfig> centralCacheTypeMap;

    public TedissonCacheManagerBase() {
        centralCacheTypeMap = new HashMap<>();
        if (isRedisEnabled()) {
            log.info("Tedisson redis is enabled");
        } else {
            log.info("Tedisson redis is disabled and local cache is used");
        }
    }

    public abstract void createCache(String cacheName, CacheConfig cacheConfig);

    /**
     * @param cacheName Cache name
     */
    public void createCache(String cacheName) {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(Integer.MAX_VALUE);
        createCache(cacheName, cacheConfig);
    }

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @param value     Item value
     */
    public void addItemToCache(String cacheName, String key, Object value) {
        CacheExpiryPolicy cacheExpiryPolicy = getCacheExpirationConfig(cacheName);
        if (cacheExpiryPolicy != null) {
            addItemToCache(cacheName, key, value, cacheExpiryPolicy.getTimeToLiveSecond(),
                    cacheExpiryPolicy.getTimeToIdleSecond(), TimeUnit.SECONDS);
        } else {
            addItemToCache(cacheName, key, value, null, null, TimeUnit.SECONDS);
        }
    }

    /**
     * @param cacheName  Cache name
     * @param key        Item key
     * @param value      Item value
     * @param timeToLive Item time to live. Item expired when time to live is overed and removed listener raised when
     *                   next time item get from cache
     * @param timeUnit   Time unit for time to live and time to idle
     */
    public void addItemToCache(String cacheName, String key, Object value, Long timeToLive, TimeUnit timeUnit) {
        CacheExpiryPolicy cacheExpiryPolicy = getCacheExpirationConfig(cacheName);
        if (cacheExpiryPolicy != null) {
            addItemToCache(cacheName, key, value, cacheExpiryPolicy.getTimeToLiveSecond(),
                    cacheExpiryPolicy.getTimeToIdleSecond(), TimeUnit.SECONDS);
        } else {
            addItemToCache(cacheName, key, value, timeToLive, null, timeUnit);
        }
    }

    /**
     * @param cacheName         Cache name
     * @param cacheExpiryPolicy Time to live and time to idle of object
     */
    public void addCacheExpirationConfig(String cacheName, CacheExpiryPolicy cacheExpiryPolicy) {
        addItemToCache(CACHE_EXPIRY_CONFIG_NAME, cacheName, cacheExpiryPolicy);
    }

    /**
     * @param cacheName            Cache name
     * @param newCacheExpiryPolicy Time to live and time to idle of object
     */
    public abstract void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy);

    protected abstract CacheExpiryPolicy getCacheExpirationConfig(String cacheName);

    protected abstract void replaceCacheItem(String cacheName, String key, Object value);

    protected abstract void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);

    protected abstract void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);

    protected abstract Boolean isRedisEnabled();

    public void updateItemExpiration(String cacheName, String key, Long timeToLive,
                                     TimeUnit timeUnit) {
        updateItemExpiration(cacheName, key, timeToLive, null, timeUnit);
    }

    public String getInstanceID() {
        return instanceID;
    }
}
