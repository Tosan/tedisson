package com.tosan.client.redis.api;

import com.tosan.client.redis.cacheconfig.CacheConfig;
import com.tosan.client.redis.enumuration.LocalCacheProvider;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 5/31/2023
 */
public interface TedissonCacheManager {
    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @return Whether item exist in cache or not
     */
    boolean isKeyInCache(String cacheName, String key);

    /**
     * @param cacheName   Cache name
     * @param cacheConfig cache configuration
     */
    void createCache(String cacheName, CacheConfig cacheConfig);

    /**
     * @param cacheName Cache name
     *                  Create cache with max size: Integer.MAX_VALUE
     */
    void createCache(String cacheName);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @return Item value
     */
    <T> T getItemFromCache(String cacheName, String key);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @param value     Item value
     */
    void addItemToCache(String cacheName, String key, Object value);

    /**
     * @param cacheName  Cache name
     * @param key        Item key
     * @param value      Item value
     * @param timeToLive Item time to live. Item expired when time to live is overed and removed listener raised when
     *                   next time item get from cache
     * @param timeUnit   Time unit for time to live and time to idle
     */
    void addItemToCache(String cacheName, String key, Object value, Long timeToLive, TimeUnit timeUnit);

    /**
     * @param cacheName  Cache name
     * @param key        Item key
     * @param value      Item value
     * @param timeToLive Item time to live. Item expired when time to live is overed and removed listener raised when
     *                   next time item get from cache
     * @param timeToIdle Item time to idle. Item expired when time to idle is overed and removed listener raised when
     *                   next time item get from cache
     * @param timeUnit   Time unit for time to live and time to idle
     */
    void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);

    /**
     * @param cacheName Cache name
     * @param items     The collection of items should be added to cache
     */
    void addAllToCache(String cacheName, Map<String, Object> items);

    /**
     * @param cacheName  Cache name
     * @param items      The collection of items should be added to cache
     * @param timeToLive Item time to live. Item expired when time to live is overed and removed listener raised when
     *                   *                  next time item get from cache
     * @param timeUnit   Time unit for time to live
     */
    void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @param value     Item new value
     */
    void replaceCacheItem(String cacheName, String key, Object value);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     */
    void removeItemFromCache(String cacheName, String key);

    /**
     * @param cacheName Cache name
     */
    void clearCache(String cacheName);

    /**
     * @param cacheName Cache name
     * @return True if cache is empty
     */
    boolean isCacheEmpty(String cacheName);

    /**
     * @param cacheName Cache name
     * @return Cache size
     */
    long getCacheSize(String cacheName);

    /**
     * @param cacheName Cache name
     * @return Set of cache keys
     */
    Set<String> getCacheKeySet(String cacheName);

    /**
     * @param cacheName Cache name
     * @return Time to live and time to idle of item
     */
    CacheExpiryPolicy getCacheExpirationConfig(String cacheName);

    /**
     * @param cacheName         Cache name
     * @param key               Atomic item key
     * @param cacheExpiryPolicy Time to live and time to idle of item
     */
    void initializeAtomicLongCache(String cacheName, String key, CacheExpiryPolicy cacheExpiryPolicy);

    /**
     * @param cacheName Cache name
     * @param key       Atomic item key
     * @return Atomic item value
     * If atomic item does not exist start value set to 0 and 1 return
     */
    long incrementAndGetAtomicItem(String cacheName, String key);

    /**
     * Reset atomic item value to 0
     *
     * @param cacheName Cache name
     * @param key       Atomic item key
     */
    void resetAtomicItem(String cacheName, String key);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @return Atomic item value
     * If atomic item does not exist start value set to 0 and 0 return
     */
    long getAtomicValue(String cacheName, String key);

    /**
     * @param cacheName Cache name
     * @param key       Item key
     * @param value     Atomic item value
     */
    void setAtomicItem(String cacheName, String key, long value);

    /**
     * @param cacheName  Cache name
     * @param key        Atomic item key
     * @param timeToLive Item time to live. Item expired when time to live is overed
     * @param timeUnit   Time unit for time to live
     */
    void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit);

    /**
     * @param cacheName         Cache name
     * @param cacheExpiryPolicy Cache expiry policy(Time to live and Time to idle)
     */
    void addCacheExpirationConfig(String cacheName, CacheExpiryPolicy cacheExpiryPolicy);

    /**
     * @param cacheName            Cache name
     * @param newCacheExpiryPolicy New cache expiry policy(Time to live and Time to idle)
     */
    void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy);

    /**
     * @param cacheName  Cache name
     * @param key        Item key
     * @param timeToLive new Time to live
     * @param timeUnit   Time unit for Time to live
     */
    void updateItemExpiration(String cacheName, String key, Long timeToLive,
                              TimeUnit timeUnit);

    /**
     * @param cacheName  Cache name
     * @param key        Item key
     * @param timeToLive new Time to live
     * @param timeToIdle new Time to idle
     * @param timeUnit   Time unit
     */
    void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit);

    /**
     * @return Node unique instance id
     */
    String getInstanceID();

    /**
     * @return redis enable
     */
    Boolean isRedisEnabled();

    /**
     * @return local cache provider
     */
    LocalCacheProvider getLocalCacheProvider();

    /**
     * @param cacheConfigs List of cache configurations(cache name, Time to live and Cache max size)
     * @return Spring Cache Manager bean for using @Cacheable
     */
    CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs);
}
