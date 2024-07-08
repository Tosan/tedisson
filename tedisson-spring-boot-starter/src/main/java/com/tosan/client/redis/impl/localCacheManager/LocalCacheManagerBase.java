package com.tosan.client.redis.impl.localCacheManager;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.cacheconfig.LocalCacheConfig;
import com.tosan.client.redis.cacheconfig.LocalRunTimeCacheConfig;
import com.tosan.client.redis.stream.MessageQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 5/29/2023
 */
@Slf4j
public abstract class LocalCacheManagerBase {

    private final Map<String, LocalRunTimeCacheConfig> runTimeCacheConfigMap = new HashMap<>();
    protected MessageQueueManager messageQueueManager;

    public void createCache(String cacheName) {
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        cacheConfig.setMaxSize(Integer.MAX_VALUE);
        createCache(cacheName, cacheConfig);
    }

    public void createCache(String cacheName, LocalCacheConfig cacheConfig) {
        log.debug("Creating cache with name:{}", cacheName);
        if (cacheConfig != null) {
            int maxSize = cacheConfig.getMaxSize();
            if (maxSize <= 0) {
                maxSize = Integer.MAX_VALUE;
            }
            LocalRunTimeCacheConfig runTimeCacheConfig = new LocalRunTimeCacheConfig();
            runTimeCacheConfig.setNeedsClearCachePropagation(cacheConfig.getNeedsClearCachePropagation());
            runTimeCacheConfig.setExpiryPolicy(cacheConfig.getExpiryPolicy());
            runTimeCacheConfigMap.put(cacheName, runTimeCacheConfig);
            createCache(cacheName, cacheConfig.getCacheListener(), cacheConfig.getExpiryPolicy(), maxSize);
            log.debug("Cache with name '{}' created. Live={} Idle={} second, max-size={} ",
                    cacheName, cacheConfig.getExpiryPolicy() != null ? cacheConfig.getExpiryPolicy().getTimeToLiveSecond() : null,
                    cacheConfig.getExpiryPolicy() != null ? cacheConfig.getExpiryPolicy().getTimeToIdleSecond() : null, cacheConfig.getMaxSize());
        } else {
            createCache(cacheName);
        }
    }

    public void clearCache(String cacheName) {
        LocalRunTimeCacheConfig runTimeCacheConfig = getRunTimeCacheConfig(cacheName);
        if (messageQueueManager == null) {
            invalidateAllCacheItems(cacheName);
        } else if (isCacheExist(cacheName) && runTimeCacheConfig != null && runTimeCacheConfig.getNeedsClearCachePropagation()) {
            messageQueueManager.sendClearCacheMessage(cacheName);
        }
    }

    public abstract boolean isCacheExist(String cacheName);

    public abstract void invalidateAllCacheItems(String cacheName);

    public void removeCache(String cacheName) {
        runTimeCacheConfigMap.remove(cacheName);
        log.info("Cache with name '{}' removed.", cacheName);
    }

    private LocalRunTimeCacheConfig getRunTimeCacheConfig(String cacheName) {
        return runTimeCacheConfigMap.get(cacheName);
    }

    public CacheExpiryPolicy getCacheExpirationConfig(String cacheName) {
        LocalRunTimeCacheConfig runTimeCacheConfig = runTimeCacheConfigMap.get(cacheName);
        if (runTimeCacheConfig == null) {
            return null;
        }
        return runTimeCacheConfig.getExpiryPolicy();
    }

    public void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy) {
        LocalRunTimeCacheConfig runTimeCacheConfig = runTimeCacheConfigMap.get(cacheName);
        if (runTimeCacheConfig != null) {
            runTimeCacheConfig.setExpiryPolicy(newCacheExpiryPolicy);
            runTimeCacheConfigMap.replace(cacheName, runTimeCacheConfig);
            log.info("Cache '{}' configs changed. Live={} Idle={}",
                    cacheName, newCacheExpiryPolicy.getTimeToLiveSecond(), newCacheExpiryPolicy.getTimeToIdleSecond());
        }
    }

    protected abstract void createCache(String cacheName, CacheListener listener, CacheExpiryPolicy cacheExpiryPolicy, long heapSize);

    protected abstract void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, TimeUnit timeUnit);

    protected abstract void addItemToCache(String cacheName, Object key, Object value);

    public void addAllToCache(String cacheName, Map<String, Object> items) {
        if (MapUtils.isNotEmpty(items)) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                addItemToCache(cacheName, entry.getKey(), entry.getValue());
            }
        }
    }

    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        if (MapUtils.isNotEmpty(items)) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                addItemToCache(cacheName, entry.getKey(), entry.getValue(), timeToLive, timeUnit);
            }
        }
    }

    public void setMessageQueueManager(MessageQueueManager messageQueueManager) {
        this.messageQueueManager = messageQueueManager;
    }
}
