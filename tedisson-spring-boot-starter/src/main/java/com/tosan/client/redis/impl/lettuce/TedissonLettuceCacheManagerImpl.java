package com.tosan.client.redis.impl.lettuce;

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
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncCreatedListener;
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncRemovedListener;
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncUpdatedListener;
import com.tosan.client.redis.stream.MessageQueueManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Lettuce-based implementation of TedissonCacheManager
 * Uses Spring Data Redis for cache operations
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class TedissonLettuceCacheManagerImpl extends TedissonCacheManagerBase implements TedissonCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private LocalCacheManager localCacheManager;
    private LettuceSyncCreatedListener createdSyncListener;
    private LettuceSyncUpdatedListener updatedSyncListener;
    private LettuceSyncRemovedListener removedSyncListener;
    private boolean messageQueueEnable = false;
    private MessageQueueManager messageQueueManager;

    public TedissonLettuceCacheManagerImpl(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.redisTemplate = createRedisTemplate(connectionFactory);
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    @Override
    public boolean isKeyInCache(String cacheName, String key) {
        if (key == null) {
            return false;
        }
        String hashKey = cacheName + ":" + key;
        Object result = redisTemplate.opsForValue().get(hashKey);
        return result != null;
    }

    @Override
    public void createCache(String cacheName, CacheConfig cacheConfig) {
        if (cacheConfig != null) {
            createCache(cacheName, cacheConfig.getListeners(), cacheConfig.getExpiryPolicy(),
                    cacheConfig.getMaxSize(), cacheConfig.getCentralCacheType());
        } else {
            createCache(cacheName);
        }
    }

    @Override
    public void replaceCacheExpirationConfig(String cacheName, CacheExpiryPolicy newCacheExpiryPolicy) {
        replaceCacheItem(CACHE_EXPIRY_CONFIG_NAME, cacheName, newCacheExpiryPolicy);
        log.info("Cache '{}' configs changed. Live={} Idle={}",
                cacheName, newCacheExpiryPolicy.getTimeToLiveSecond(),
                newCacheExpiryPolicy.getTimeToIdleSecond());
    }

    public void createCache(String cacheName, List<CacheListener> listeners, CacheExpiryPolicy cacheExpiryPolicy,
                            int maxSize, CentralCacheTypeConfig centralCacheTypeConfig) {
        // In Lettuce, caches are created on demand, just register the configuration
        if (cacheExpiryPolicy != null) {
            addCacheExpirationConfig(cacheName, cacheExpiryPolicy);
        }

        // Add listeners if provided
        if (CollectionUtils.isNotEmpty(listeners)) {
            for (CacheListener listener : listeners) {
                addCustomCacheListener(cacheName, listener);
            }
        }

        if (centralCacheTypeConfig != null && centralCacheTypeConfig.getCentralCacheType() != null) {
            setCacheType(cacheName, centralCacheTypeConfig);
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
                        throw new TedissonRuntimeException("message queue should be enable for creating StreamSyncedLocalCacheConfig");
                    }
                    localCacheManager.createCache(cacheName, cacheConfig);
                    break;
            }
        }

        log.debug("Lettuce Cache with name '{}' created. Live={} Idle={} second, max size={}",
                cacheName, cacheExpiryPolicy != null ? cacheExpiryPolicy.getTimeToLiveSecond() : null,
                cacheExpiryPolicy != null ? cacheExpiryPolicy.getTimeToIdleSecond() : null, maxSize);
    }

    @Override
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

    @Override
    public <T> T getItemFromHash(String key) {
        if (key == null) {
            return null;
        }
        LettuceCacheElement cacheElement = (LettuceCacheElement) redisTemplate.opsForValue().get(key);
        if (cacheElement != null && cacheElement.getData() != null) {
            return (T) cacheElement.getData();
        }
        return null;
    }

    @Override
    public void addItemsToCache(String cacheName, Map<String, Object> items) {
        if (MapUtils.isEmpty(items)) {
            return;
        }
        Map<String, LettuceCacheElement> cacheItems = new HashMap<>();
        items.forEach((k, v) -> cacheItems.put(k, new LettuceCacheElement(v, instanceID)));
        redisTemplate.opsForHash().putAll(cacheName, cacheItems);
    }

    @Override
    public void addItemsToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        addItemsToCache(cacheName, items);
        if (timeToLive != null && timeToLive > 0) {
            redisTemplate.expire(cacheName, timeToLive, timeUnit);
        }
    }

    private Long min(Long a, Long b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }

    public void insertIntoCentralCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        String hashKey = cacheName + ":" + key;
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        LettuceCacheElement cacheElement = new LettuceCacheElement(value, instanceID);
        if (timeToIdle == null && timeToLive == null) {
            valueOps.set(hashKey, cacheElement);
        } else {
            if (timeToLive != null) {
                long ttlMillis = timeUnit.toMillis(timeToLive);
                long absoluteExpireAt = System.currentTimeMillis() + ttlMillis;
                cacheElement.setExpirationTime(absoluteExpireAt);
            }
            if (timeToIdle != null) {
                cacheElement.setTimeToIdle(timeUnit.toMillis(timeToIdle));
            }
            valueOps.set(hashKey, cacheElement, Duration.ofSeconds(min(timeToIdle, timeToLive)));
        }
    }

    @Override
    public void addItemToCache(String cacheName, String key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {

        insertIntoCentralCache(cacheName, key, value, timeToLive, timeToIdle, timeUnit);
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.LISTENER_SYNCED_LOCAL) {
            // Publish cache created event
            LettuceCacheElement cacheElement = new LettuceCacheElement(value, instanceID);
            redisTemplate.convertAndSend("cache:created:" + cacheName + ":" + key, cacheElement);
            localCacheManager.addItemToCache(cacheName, key, value, timeToLive, timeToIdle, timeUnit);
        } else if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        }
    }

    @Override
    public void addItemsToCache(String cacheName, Map<String, Object> items, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        addItemsToCache(cacheName, items, timeToLive, timeUnit);
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items) {
        Map<String, LettuceCacheElement> cachedItems = convertMapItems(items);
        if (MapUtils.isNotEmpty(cachedItems)) {
            for (Map.Entry<String, LettuceCacheElement> entry : cachedItems.entrySet()) {
                String hashKey = cacheName + ":" + entry.getKey();
                redisTemplate.opsForValue().set(hashKey, entry.getValue());
            }
            CentralCacheType centralCacheType = getCacheType(cacheName);
            if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
                sendCacheClearMessage(cacheName);
            }
        }
    }

    @Override
    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        Map<String, LettuceCacheElement> cachedItems = convertMapItems(items);
        if (MapUtils.isNotEmpty(cachedItems)) {
            redisTemplate.opsForHash().putAll(cacheName, cachedItems);
            if (timeToLive != null && timeToLive > 0) {
                redisTemplate.expire(cacheName, timeToLive, timeUnit);
            }
            CentralCacheType centralCacheType = getCacheType(cacheName);
            if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
                sendCacheClearMessage(cacheName);
            }
        }
    }

    @Override
    public void addItemToHash(String key, Object value) {
        LettuceCacheElement cacheElement = new LettuceCacheElement(value, instanceID);
        redisTemplate.opsForValue().set(key, cacheElement);
    }

    @Override
    public void addItemToHash(String key, Object value, Long timeToLive, TimeUnit timeUnit) {
        LettuceCacheElement cacheElement = new LettuceCacheElement(value, instanceID);
        redisTemplate.opsForValue().set(key, cacheElement, Duration.of(timeToLive, timeUnit.toChronoUnit()));
    }

    @Override
    public void addItemsToHash(Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        items.forEach((key, value) -> addItemToHash(key, value, timeToLive, timeUnit));
    }

    @Override
    public void removeItemFromHash(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void replaceHashItem(String key, Object value) {
        LettuceCacheElement cacheElement = new LettuceCacheElement(value, instanceID);
        redisTemplate.opsForValue().set(key, cacheElement);
    }

    @Override
    public boolean isKeyInHash(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void expireCache(String cacheName, Long timeToLive, TimeUnit timeUnit) {
        redisTemplate.expire(cacheName, timeToLive, timeUnit);
    }

    @Override
    public void expireCacheAsync(String cacheName, Long timeToLive, TimeUnit timeUnit) {
        // Lettuce doesn't provide async operations in the same way, just call sync
        expireCache(cacheName, timeToLive, timeUnit);
    }

    @Override
    public void removeItemFromCache(String cacheName, String key) {
        if (key == null || cacheName == null) {
            return;
        }
        String hashKey = cacheName + ":" + key;
        redisTemplate.opsForValue().getAndDelete(hashKey);
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        } else if (centralCacheType == CentralCacheType.LISTENER_SYNCED_LOCAL) {
            // Publish cache removed event
            redisTemplate.convertAndSend("cache:removed:" + cacheName + ":" + key, "");
            localCacheManager.removeItemFromCache(cacheName, key);
        }
    }

    @Override
    public void clearCache(String cacheName) {
        String pattern = cacheName + ":*";
        Set<String> values = redisTemplate.keys(pattern);
        redisTemplate.delete(values);
    }

    @Override
    public boolean isCacheEmpty(String cacheName) {
        String pattern = cacheName + ":*";
        Set<String> values = redisTemplate.keys(pattern);
        Set<String> keys = Optional.of(values)
                .orElse(Collections.emptySet())
                .stream()
                .map(s -> s.substring(s.indexOf(':') + 1))
                .collect(Collectors.toSet());
        return keys.isEmpty();
    }

    @Override
    public long getCacheSize(String cacheName) {
        String pattern = cacheName + ":*";
        Set<String> values = redisTemplate.keys(pattern);
        Set<String> keys = Optional.of(values)
                .orElse(Collections.emptySet())
                .stream()
                .map(s -> s.substring(s.indexOf(':') + 1))
                .collect(Collectors.toSet());
        return keys.size();
    }

    @Override
    public Set<String> getCacheKeySet(String cacheName) {
        String pattern = cacheName + ":*";
        Set<String> values = redisTemplate.keys(pattern);
        return Optional.of(values)
                .orElse(Collections.emptySet())
                .stream()
                .map(s -> s.substring(s.indexOf(':') + 1))
                .collect(Collectors.toSet());
    }

    @Override
    public CacheExpiryPolicy getCacheExpirationConfig(String cacheName) {
        LettuceCacheElement element = (LettuceCacheElement) redisTemplate.opsForValue().get(CACHE_EXPIRY_CONFIG_NAME + ":" + cacheName);
        return element != null ? (CacheExpiryPolicy) element.getData() : null;
    }

    @Override
    public void initializeAtomicLongCache(String cacheName, String key, CacheExpiryPolicy cacheExpiryPolicy) {
        String atomicKey = getAtomicKey(cacheName, key);
        if (!isKeyInHash(atomicKey)) {
            redisTemplate.opsForValue().set(atomicKey, 0L);
            if (cacheExpiryPolicy != null && cacheExpiryPolicy.getTimeToLiveSecond() > 0) {
                redisTemplate.expire(atomicKey, cacheExpiryPolicy.getTimeToLiveSecond(), TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public long incrementAndGetAtomicItem(String cacheName, String key) {
        String atomicKey = getAtomicKey(cacheName, key);
        // Use Redis increment command for atomic increment
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Long result = ops.increment(atomicKey);
        if (result == null) {
            setAtomicItem(cacheName, key, 1L);
        }
        return result;
    }

    @Override
    public void resetAtomicItem(String cacheName, String key) {
        String atomicKey = getAtomicKey(cacheName, key);
        redisTemplate.delete(atomicKey);
    }

    @Override
    public long getAtomicValue(String cacheName, String key) {
        String atomicKey = getAtomicKey(cacheName, key);
        Object current = redisTemplate.opsForValue().get(atomicKey);
        return current == null ? 0 : ((Number) current).longValue();
    }

    @Override
    public void setAtomicItem(String cacheName, String key, long value) {
        String atomicKey = getAtomicKey(cacheName, key);
        redisTemplate.opsForValue().set(atomicKey, value);
    }

    @Override
    public void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit) {
        String atomicKey = getAtomicKey(cacheName, key);
        redisTemplate.expire(atomicKey, timeToLive, timeUnit);
    }

    @Override
    public void addCacheExpirationConfig(String cacheName, CacheExpiryPolicy cacheExpiryPolicy) {
        String configKey = CACHE_EXPIRY_CONFIG_NAME + ":" + cacheName;
        redisTemplate.opsForValue().set(configKey, new LettuceCacheElement(cacheExpiryPolicy, instanceID));
    }

    @Override
    public void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        if (timeToIdle != null || timeToLive != null) {
            String hashKey = cacheName + ":" + key;
            Object raw = redisTemplate.opsForValue().get(hashKey);
            LettuceCacheElement cacheElement = null;
            if (raw != null) {
                cacheElement = (LettuceCacheElement) raw;
            }
            if (cacheElement != null) {
                Long ttlMillis = null;
                if (timeToLive != null) {
                    ttlMillis = timeUnit.toMillis(timeToLive);
                }
                Long ttiMillis = null;
                if (timeToIdle != null) {
                    ttiMillis = timeUnit.toMillis(timeToIdle);
                    cacheElement.setTimeToIdle(ttiMillis);
                }
                cacheElement.setExpirationTime(System.currentTimeMillis() + min(ttlMillis, ttiMillis));
                redisTemplate.opsForValue().set(hashKey, cacheElement, Duration.ofSeconds(min(timeToIdle, timeToLive)));
            }
        }
    }

    @Override
    public void replaceCacheItem(String cacheName, String key, Object value) {
        String hashKey = cacheName + ":" + key;
        Object raw = redisTemplate.opsForValue().get(hashKey);
        LettuceCacheElement cacheElement = null;
        if (raw != null) {
            cacheElement = (LettuceCacheElement) raw;
        }
        if (cacheElement != null) {
            cacheElement.setData(value);
        }
        redisTemplate.opsForValue().set(hashKey, cacheElement);
        // Publish cache updated event
        CentralCacheType centralCacheType = getCacheType(cacheName);
        if (centralCacheType == CentralCacheType.STREAM_SYNCED_LOCAL) {
            sendCacheClearMessage(cacheName);
        } else if (centralCacheType == CentralCacheType.LISTENER_SYNCED_LOCAL) {
            redisTemplate.convertAndSend("cache:updated:" + cacheName + ":" + key, cacheElement);
            localCacheManager.replaceCacheItem(cacheName, key, value);
        }
    }

    @Override
    public CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory);

        if (CollectionUtils.isNotEmpty(cacheConfigs)) {
            for (SpringCacheConfig cacheConfig : cacheConfigs) {
                RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMillis(cacheConfig.getTimeToLive() * 1000));
                builder.withCacheConfiguration(cacheConfig.getCacheName(), cacheConfiguration);
            }
        }

        return builder.build();
    }

    @Override
    public Boolean isRedisEnabled() {
        return true;
    }

    @Override
    public LocalCacheProvider getLocalCacheProvider() {
        return localCacheManager != null ? localCacheManager.getCacheProvider() : null;
    }

    @Override
    public String getInstanceID() {
        return instanceID;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFromCentralCache(String cacheName, String key) {
        String hashKey = cacheName + ":" + key;
        Object raw = redisTemplate.opsForValue().get(hashKey);
        LettuceCacheElement cacheElement = raw instanceof LettuceCacheElement ? (LettuceCacheElement) raw : null;
        if (cacheElement != null && cacheElement.getData() != null) {
            if (cacheElement.getExpirationTime() != null) {
                Long absoluteExpireAt = cacheElement.getExpirationTime();
                long remaining = absoluteExpireAt - System.currentTimeMillis();
                if (remaining <= 0) {
                    redisTemplate.delete(hashKey);
                    return null;
                }
                long newExpire = min(remaining, cacheElement.getTimeToIdle());
                redisTemplate.expire(hashKey, Duration.ofMillis(newExpire));
            }
            return (T) cacheElement.getData();
        }
        return null;
    }

    private <T> T getFromManagedCache(String cacheName, String key) {
        if (key == null) {
            return null;
        }
        if (localCacheManager.isKeyInCache(cacheName, key)) {
            return localCacheManager.getItemFromCache(cacheName, key);
        } else {
            LettuceCacheElement value = getFromCentralCache(cacheName, key);
            if (value != null) {
                long ttl = getRemainingTtl(value);
                if (ttl > 0) {
                    localCacheManager.addItemToCache(cacheName, key, value, ttl, null, TimeUnit.MILLISECONDS);
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

    private long getRemainingTtl(LettuceCacheElement cacheElement) {
        return cacheElement.getExpirationTime();
    }

    private String getAtomicKey(String cacheName, String key) {
        return "atomic:" + cacheName + ":" + key;
    }

    public void addCustomCacheListener(String cacheName, CacheListener listener) {
        if (connectionFactory != null && listener != null) {
            RedisMessageListenerContainer container =
                    new RedisMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.addMessageListener((MessageListener) listener,
                    new PatternTopic("__keyspace@*__:" + cacheName + ":*"));
            try {
                container.afterPropertiesSet();
                container.start();
            } catch (Exception e) {
                log.warn("Error starting message container for cache listener", e);
            }
        }
    }

    public void addCacheListener(String cacheName, CacheListener listener) {
        if (connectionFactory != null) {
            if (listener instanceof LettuceSyncCreatedListener) {
                RedisMessageListenerContainer container =
                        new RedisMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.addMessageListener((MessageListener) listener,
                        new PatternTopic("cache:created:" + cacheName + ":*"));
                try {
                    container.afterPropertiesSet();
                    container.start();
                } catch (Exception e) {
                    log.warn("Error starting message container for cache listener", e);
                }
            } else if (listener instanceof LettuceSyncUpdatedListener) {
                RedisMessageListenerContainer container =
                        new RedisMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.addMessageListener((MessageListener) listener,
                        new PatternTopic("cache:updated:" + cacheName + ":*"));
                try {
                    container.afterPropertiesSet();
                    container.start();
                } catch (Exception e) {
                    log.warn("Error starting message container for cache listener", e);
                }
            } else if (listener instanceof LettuceSyncRemovedListener) {
                RedisMessageListenerContainer container =
                        new RedisMessageListenerContainer();
                container.setConnectionFactory(connectionFactory);
                container.addMessageListener((MessageListener) listener,
                        new PatternTopic("cache:removed:" + cacheName + ":*"));
                try {
                    container.afterPropertiesSet();
                    container.start();
                } catch (Exception e) {
                    log.warn("Error starting message container for cache listener", e);
                }
            }
        }
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

    private boolean hasCacheRemovedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig =
                    (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedRemovedListener();
        }
        return false;
    }

    private boolean hasCacheUpdatedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig =
                    (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedUpdatedListener();
        }
        return false;
    }

    private boolean hasCacheCreatedListener(CentralCacheTypeConfig centralCacheTypeConfig) {
        if (centralCacheTypeConfig instanceof ListenerSyncedLocalCacheConfig) {
            ListenerSyncedLocalCacheConfig syncedLocalCacheConfig =
                    (ListenerSyncedLocalCacheConfig) centralCacheTypeConfig;
            return syncedLocalCacheConfig.isNeedCreatedListener();
        }
        return false;
    }

    private void sendCacheClearMessage(String cacheName) {
        if (messageQueueEnable && messageQueueManager != null) {
            messageQueueManager.sendClearCacheMessage(cacheName);
            localCacheManager.clearCache(cacheName);
        }
    }

    private Map<String, LettuceCacheElement> convertMapItems(Map<String, Object> items) {
        Map<String, LettuceCacheElement> cachedItems = new HashMap<>();
        if (items != null) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                cachedItems.put(entry.getKey(), new LettuceCacheElement(entry.getValue(), instanceID));
            }
        }
        return cachedItems;
    }
}