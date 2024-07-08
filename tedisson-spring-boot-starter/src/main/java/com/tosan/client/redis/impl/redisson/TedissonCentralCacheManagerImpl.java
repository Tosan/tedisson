package com.tosan.client.redis.impl.redisson;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.SpringCacheConfig;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import com.tosan.client.redis.cacheconfig.CentralCacheTypeConfig;
import com.tosan.client.redis.cacheconfig.ListenerSyncedLocalCacheConfig;
import com.tosan.client.redis.cacheconfig.LocalCacheConfig;
import com.tosan.client.redis.enumuration.CentralCacheType;
import com.tosan.client.redis.enumuration.LocalCacheProvider;
import com.tosan.client.redis.exception.TedissonRuntimeException;
import com.tosan.client.redis.impl.TedissonCacheManagerBase;
import com.tosan.client.redis.impl.redisson.listener.TedissonCreatedSyncListener;
import com.tosan.client.redis.impl.redisson.listener.TedissonRemovedSyncListener;
import com.tosan.client.redis.impl.redisson.listener.TedissonUpdatedSyncListener;
import com.tosan.client.redis.stream.MessageQueueManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.event.MapEntryListener;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 06/01/2021
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class TedissonCentralCacheManagerImpl extends TedissonCacheManagerBase implements TedissonCacheManager {

    private final RedissonClient redisClient;
    private LocalCacheManager localCacheManager;
    private TedissonUpdatedSyncListener updatedSyncListener;
    private TedissonRemovedSyncListener removedSyncListener;
    private TedissonCreatedSyncListener createdSyncListener;
    @Lazy
    private MessageQueueManager messageQueueManager;
    private boolean messageQueueEnable = false;

    public TedissonCentralCacheManagerImpl(RedissonClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public boolean isKeyInCache(String cacheName, String key) {
        if (key == null) {
            return false;
        }
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        return map.containsKey(key);
    }

    @Override
    public void createCache(String cacheName, CacheConfig cacheConfig) {
        if (cacheConfig != null) {
            createCache(cacheName, cacheConfig.getListeners(), cacheConfig.getExpiryPolicy(), cacheConfig.getMaxSize(), cacheConfig.getCentralCacheType());
        } else {
            createCache(cacheName);
        }
    }

    @Override
    public void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy) {
        replaceCacheItem(CACHE_EXPIRY_CONFIG_NAME, cacheName, newCacheExpiryPolicy);
        log.info("Cache '{}' configs changed. Live={} Idle={}",
                cacheName, newCacheExpiryPolicy.getTimeToLiveSecond(), newCacheExpiryPolicy.getTimeToIdleSecond());
    }

    public void createCache(String cacheName, List<CacheListener> listener, CacheExpiryPolicy cacheExpiryPolicy,
                            int maxSize, CentralCacheTypeConfig centralCacheTypeConfig) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        map.setMaxSize(maxSize);
        if (CollectionUtils.isNotEmpty(listener)) {
            for (CacheListener cacheListener : listener) {
                addCacheListener(cacheName, cacheListener);
            }
        }
        if (cacheExpiryPolicy != null) {
            addCacheExpirationConfig(cacheName, cacheExpiryPolicy);
        }
        if (centralCacheTypeConfig != null && centralCacheTypeConfig.getCentralCacheType() != null) {
            LocalCacheConfig cacheConfig = new LocalCacheConfig();
            cacheConfig.setMaxSize(maxSize);
            cacheConfig.setExpiryPolicy(cacheExpiryPolicy);
            switch (centralCacheTypeConfig.getCentralCacheType()) {
                case LISTENER_SYNCED_LOCAL:
                    addListenerForSyncingLocalCache(cacheName, centralCacheTypeConfig);
                    localCacheManager.createCache(cacheName, cacheConfig);
                    break;
                case STREAM_SYNCED_LOCAL:
                    if (!messageQueueEnable) {
                        throw new TedissonRuntimeException("messeage queue should be enable for creating StreamSyncedLocalCacheConfig");
                    }
                    localCacheManager.createCache(cacheName, cacheConfig);
            }
        }
        log.debug("Centralized Cache with name '{}' created. Live={} Idle={} second, max size={} ",
                cacheName, cacheExpiryPolicy != null ? cacheExpiryPolicy.getTimeToLiveSecond() : null, cacheExpiryPolicy != null ?
                        cacheExpiryPolicy.getTimeToIdleSecond() : null, maxSize);
    }

    public <T> T getItemFromCache(String cacheName, String key) {
        if (key == null) {
            return null;
        }
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == null) {
            return getFromCentralCache(cacheName, key);
        }
        switch (centralCacheType) {
            case SHARED:
                return getFromCentralCache(cacheName, key);
            case LISTENER_SYNCED_LOCAL:
            case STREAM_SYNCED_LOCAL:
                return getFromManagedCache(cacheName, key);
        }
        return null;
    }

    public void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        CentralCacheType centralCacheType = getCacheType(cacheName);
        insertIntoCentralCache(cacheName, key, value, timeToLive, timeToIdle, timeUnit);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        }
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items) {
        Map<String, CacheElement> cachedItems = convertMapItems(items);
        if (MapUtils.isNotEmpty(cachedItems)) {
            RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
            CentralCacheType centralCacheType = getCacheType(cacheName);
            map.putAll(cachedItems);
            if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
                sendCacheClearMessage(cacheName);
            }
        }
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        Map<String, CacheElement> cachedItems = convertMapItems(items);
        if (MapUtils.isNotEmpty(cachedItems)) {
            RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
            CentralCacheType centralCacheType = getCacheType(cacheName);
            map.putAll(cachedItems, timeToLive, timeUnit);
            if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
                sendCacheClearMessage(cacheName);
            }
        }
    }

    private Map<String, CacheElement> convertMapItems(Map<String, Object> items) {
        Map<String, CacheElement> cachedItems = new HashMap<>();
        if (items != null) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                cachedItems.put(entry.getKey(), new CacheElement(entry.getValue(), instanceID));
            }
        }
        return cachedItems;
    }

    public void insertIntoCentralCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        if (timeToLive == null) {
            timeToLive = 0L;
        }
        if (timeToIdle == null) {
            timeToIdle = 0L;
        }
        map.fastPut(key, new CacheElement(value, instanceID), timeToLive, timeUnit, timeToIdle, timeUnit);
    }

    public void replaceCacheItem(String cacheName, String key, Object value) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        map.fastReplace(key, new CacheElement(value, instanceID));
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        } else if (centralCacheType == CentralCacheType.LISTENER_SYNCED_LOCAL) {
            localCacheManager.replaceCacheItem(cacheName, key, value);
        }
    }

    public void removeItemFromCache(String cacheName, String key) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        map.remove(key);
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        } else if (centralCacheType == CentralCacheType.LISTENER_SYNCED_LOCAL) {
            localCacheManager.removeItemFromCache(cacheName, key);
        }
    }

    @Override
    public void clearCache(String cacheName) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        map.clear();
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        }
    }

    @Override
    public boolean isCacheEmpty(String cacheName) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        return map.isEmpty();
    }

    public long getCacheSize(String cacheName) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        return map.size();
    }

    @Override
    public Set<String> getCacheKeySet(String cacheName) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        return map.keySet();
    }

    public void addCacheListener(String cacheName, CacheListener mapListener) {
        if (redisClient != null && (mapListener instanceof MapEntryListener)) {
            RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
            map.addListener((MapEntryListener) mapListener);
        }
    }

    public CacheExpiryPolicy getCacheExpirationConfig(String cacheName) {
        return getItemFromCache(CACHE_EXPIRY_CONFIG_NAME, cacheName);
    }

    @Override
    public void initializeAtomicLongCache(String cacheName, String key, CacheExpiryPolicy cacheExpiryPolicy) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        atomicLong.set(0L);
        atomicLong.expire(Duration.ofSeconds(cacheExpiryPolicy.getTimeToLiveSecond()));
    }

    @Override
    public long incrementAndGetAtomicItem(String cacheName, String key) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        return atomicLong.addAndGet(1L);
    }

    @Override
    public void resetAtomicItem(String cacheName, String key) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        atomicLong.set(0L);
    }

    @Override
    public long getAtomicValue(String cacheName, String key) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        return atomicLong.get();
    }

    @Override
    public void setAtomicItem(String cacheName, String key, long value) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        atomicLong.set(value);
    }

    @Override
    public void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit) {
        RAtomicLong atomicLong = redisClient.getAtomicLong(getAtomicCacheKey(cacheName, key));
        atomicLong.expire(Duration.ofSeconds(TimeUnit.SECONDS.convert(timeToLive, timeUnit)));
    }

    @Override
    public void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        if (timeToLive == null) {
            timeToLive = 0L;
        }
        if (timeToIdle == null) {
            timeToIdle = 0L;
        }
        map.updateEntryExpiration(key, timeToLive, timeUnit, timeToIdle, timeUnit);
    }

    @Override
    public Boolean isRedisEnabled() {
        return true;
    }

    @Override
    public LocalCacheProvider getLocalCacheProvider() {
        return localCacheManager.getCacheProvider();
    }

    private org.redisson.spring.cache.CacheConfig getSpringCacheConfiguration(SpringCacheConfig springCacheConfig) {
        org.redisson.spring.cache.CacheConfig config = new org.redisson.spring.cache.CacheConfig();
        config.setTTL(springCacheConfig.getTimeToLive() * 1000);
        config.setMaxSize(Math.toIntExact(springCacheConfig.getMaxSize()));
        return config;
    }

    @Override
    public CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs) {
        if (CollectionUtils.isNotEmpty(cacheConfigs)) {
            Map<String, org.redisson.spring.cache.CacheConfig> configMap = new HashMap<>();
            for (SpringCacheConfig cacheConfig : cacheConfigs) {
                configMap.put(cacheConfig.getCacheName(), getSpringCacheConfiguration(cacheConfig));
            }
            return new RedissonSpringCacheManager(redisClient, configMap);
        }
        return new RedissonSpringCacheManager(redisClient);
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromCentralCache(String cacheName, String key) {
        if (key == null) {
            return null;
        }
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        CacheElement cacheElement = map.get(key);
        if (cacheElement != null && cacheElement.getData() != null) {
            return (T) cacheElement.getData();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromManagedCache(String cacheName, String key) {
        if (key == null) {
            return null;
        }
        if (localCacheManager.isKeyInCache(cacheName, key)) {
            return localCacheManager.getItemFromCache(cacheName, key);
        } else {
            Object value = getFromCentralCache(cacheName, key);
            if (value != null) {
                long ttl = getRemainingTtl(cacheName, key);
                if (ttl > 0) {
                    localCacheManager.addItemToCache(cacheName, key, value, ttl, null, TimeUnit.SECONDS);
                } else {
                    localCacheManager.addItemToCache(cacheName, key, value);
                }
            }
            if (value != null) {
                return (T) value;
            }
            return null;
        }
    }

    private long getRemainingTtl(String cacheName, String key) {
        RMapCache<String, CacheElement> map = redisClient.getMapCache(cacheName);
        return map.remainTimeToLive(key);
    }

    private CentralCacheType getCacheType(String cacheName) {
        return centralCacheTypeMap.get(cacheName) != null ? centralCacheTypeMap.get(cacheName).getCentralCacheType() : null;
    }

    private boolean hasCacheRemovedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig = (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedRemovedListener();
        }
        return false;
    }

    private boolean hasCacheUpdatedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig = (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedUpdatedListener();
        }
        return false;
    }

    private boolean hasCacheCreatedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig = (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedCreatedListener();
        }
        return false;
    }

    private void addListenerForSyncingLocalCache(String cacheName, CentralCacheTypeConfig centralCacheTypeConfig) {
        if (localCacheManager.isCacheEmpty(cacheName)) {
            if (hasCacheRemovedListener(centralCacheTypeConfig)) {
                addCacheListener(cacheName, removedSyncListener);
            }
            if (hasCacheUpdatedListener(centralCacheTypeConfig)) {
                addCacheListener(cacheName, updatedSyncListener);
            }
            if (hasCacheCreatedListener(centralCacheTypeConfig)) {
                addCacheListener(cacheName, createdSyncListener);
            }
        }
    }

    private void sendCacheClearMessage(String cacheName) {
        if (messageQueueEnable) {
            messageQueueManager.sendClearCacheMessage(cacheName);
            localCacheManager.clearCache(cacheName);
        }
    }

    private String getAtomicCacheKey(String cacheName, String key) {
        return cacheName + "_" + key;
    }
}
