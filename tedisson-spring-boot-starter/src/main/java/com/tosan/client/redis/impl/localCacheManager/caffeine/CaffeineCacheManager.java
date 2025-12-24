package com.tosan.client.redis.impl.localCacheManager.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.SpringCacheConfig;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.api.listener.CaffeineCacheListener;
import com.tosan.client.redis.enumuration.LocalCacheProvider;
import com.tosan.client.redis.exception.TedissonRuntimeException;
import com.tosan.client.redis.impl.localCacheManager.LocalCacheManagerBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author R.Mehri
 * @since 2/9/2022
 */
@Slf4j
public class CaffeineCacheManager extends LocalCacheManagerBase implements LocalCacheManager {

    private final Map<String, Cache<String, CaffeineElement>> manager = new HashMap<>();

    public CaffeineCacheManager() {
    }

    public void createCache(String cacheName, CacheListener listener, CacheExpiryPolicy cacheExpiryPolicy, long heapSize) {
        if (!isCacheExist(cacheName)) {
            manager.put(cacheName, getCacheWithConfig((CaffeineCacheListener) listener, heapSize));
        }
    }

    @Override
    public void invalidateAllCacheItems(String cacheName) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.invalidateAll();
            log.info("cache cleared : " + cacheName);
        } else {
            log.error("cache not found : " + cacheName);
        }
    }

    public void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            CaffeineElement caffeineElement = cache.getIfPresent(key);
            if (caffeineElement != null) {
                if (timeToLive != null) {
                    caffeineElement.setExpirationTimeNano(System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeToLive, timeUnit));
                    caffeineElement.setTimeToLiveSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
                    caffeineElement.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
                } else {
                    caffeineElement.setTimeToLiveSecond(null);
                }
                if (timeToLive == null && timeToIdle != null) {
                    caffeineElement.setExpirationTimeNano(System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeToIdle, timeUnit));
                }
                if (timeToIdle != null) {
                    caffeineElement.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToIdle, timeUnit));
                }
                replaceCacheItem(cacheName, key, caffeineElement);
            }
        }
    }

    public void removeCache(String cacheName) {
        getCacheWithName(cacheName).invalidateAll();
        manager.remove(cacheName);
        super.removeCache(cacheName);
    }

    private Cache<String, CaffeineElement> getCacheWithConfig(CaffeineCacheListener listener, long heapSize) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(heapSize)
                .scheduler(Scheduler.systemScheduler());
        if (listener != null) {
            builder.removalListener(listener);
        }
        builder.expireAfter(new CaffeineExpiry());
        return builder.build();
    }

    public boolean isKeyInCache(String cacheName, String key) {
        if (key == null || cacheName == null || getCacheWithName(cacheName) == null) {
            return false;
        }
        return getItemFromCache(cacheName, key) != null;
    }

    private Cache<String, CaffeineElement> getCacheWithName(String cacheName) {
        return manager.get(cacheName);
    }

    public long incrementAndGetAtomicItem(String cacheName, String key) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            return (long) cache.asMap().compute(key, (key1, val) -> incrementAtomicItemProcess(val)).getValue();
        }
        return 1;
    }

    public void resetAtomicItem(String cacheName, String key) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.asMap().compute(key, (key1, val) -> resetAtomicItemProcess(val));
        }
    }

    @Override
    public long getAtomicValue(String cacheName, String key) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            return (long) cache.asMap().compute(key, (key1, val) -> getAtomicItemProcess(val)).getValue();
        }
        return 0;
    }

    @Override
    public void setAtomicItem(String cacheName, String key, long value) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.put(key, new CaffeineElement(value));
        }
    }

    @Override
    public void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.asMap().compute(key, (key1, val) -> updateAtomicItemExpiration(val, timeToLive, timeUnit));
        }
    }

    private CaffeineElement updateAtomicItemExpiration(CaffeineElement element, Long timeToLive, TimeUnit timeUnit) {
        if (element != null && timeToLive != null) {
            element.setExpirationTimeNano(System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeToLive, timeUnit));
            element.setTimeToLiveSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
            element.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
        }
        return element;
    }

    private CaffeineElement incrementAtomicItemProcess(CaffeineElement element) {
        if (element == null) {
            CaffeineElement newCaffeineElement = new CaffeineElement();
            newCaffeineElement.setValue(1L);
            return newCaffeineElement;
        }
        Object value = element.getValue();
        if (!(value instanceof Number)) {
            throw new TedissonRuntimeException("Value must be numeric");
        }
        element.setValue(((Number) value).longValue() + 1);
        return element;
    }

    private CaffeineElement resetAtomicItemProcess(CaffeineElement element) {
        if (element == null) {
            CaffeineElement newCaffeineElement = new CaffeineElement();
            newCaffeineElement.setValue(0L);
            return newCaffeineElement;
        }
        element.setValue(0L);
        return element;
    }

    private CaffeineElement getAtomicItemProcess(CaffeineElement element) {
        if (element == null) {
            CaffeineElement newCaffeineElement = new CaffeineElement();
            newCaffeineElement.setValue(0L);
            return newCaffeineElement;
        }
        return element;
    }

    public Set<String> getCacheKeySet(String cacheName) {
        return getCacheWithName(cacheName) != null ? getCacheWithName(cacheName).asMap().keySet() : null;
    }

    public void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, TimeUnit timeUnit) {
        addItemToCache(cacheName, key, value, timeToLive, null, timeUnit);
    }

    public void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        CaffeineElement caffeineElement = new CaffeineElement();
        if (timeToLive != null) {
            caffeineElement.setExpirationTimeNano(System.nanoTime() + TimeUnit.NANOSECONDS.convert(timeToLive, timeUnit));
            caffeineElement.setTimeToLiveSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
        }
        if (timeToIdle != null) {
            caffeineElement.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToIdle, timeUnit));
        }
        caffeineElement.setValue(value);
        Cache cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.put(key, caffeineElement);
        }
    }

    public void addAllToCache(String cacheName, Map<String, Object> items) {
        if (MapUtils.isNotEmpty(items)) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                addItemToCache(cacheName, entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAllFromCache(String cacheName) {
        Collection<CaffeineElement> caffeineElements = getCacheWithName(cacheName).asMap().values();
        List<T> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caffeineElements)) {
            List<T> values = ((List<T>) caffeineElements.stream()
                    .map(CaffeineElement::getValue).collect(Collectors.toList()));
            list.addAll(values);
            return list;
        }
        return list;
    }

    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        if (MapUtils.isNotEmpty(items)) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                addItemToCache(cacheName, entry.getKey(), entry.getValue(), timeToLive, null, timeUnit);
            }
        }
    }

    public void addItemToCache(String cacheName, Object key, Object value) {
        CacheExpiryPolicy cacheExpiryPolicy = getCacheExpirationConfig(cacheName);
        if (cacheExpiryPolicy != null) {
            addItemToCache(cacheName, key, value, cacheExpiryPolicy.getTimeToLiveSecond(), cacheExpiryPolicy.getTimeToIdleSecond(),
                    TimeUnit.SECONDS);
        } else {
            addItemToCache(cacheName, key, value, null, null, null);
        }
    }

    public boolean isCacheExist(String cacheName) {
        return getCacheWithName(cacheName) != null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getItemFromCache(String cacheName, String key) {
        if (key == null) {
            return null;
        }
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            CaffeineElement caffeineElement = cache.getIfPresent(key);
            if (caffeineElement != null && caffeineElement.getValue() != null) {
                return (T) caffeineElement.getValue();
            }
        }
        return null;
    }

    public void removeItemFromCache(String cacheName, String key) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    private void replaceCacheItem(String cacheName, String key, CaffeineElement caffeineElement) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.put(key, caffeineElement);
        }
    }

    public void replaceCacheItem(String cacheName, String key, Object value) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            CaffeineElement oldCaffeineElement = cache.getIfPresent(key);
            if (oldCaffeineElement != null) {
                CaffeineElement caffeineElement = new CaffeineElement(oldCaffeineElement.getTimeToLiveSecond(), oldCaffeineElement.getTimeToIdleSecond(),
                        oldCaffeineElement.getExpirationTimeNano(), value);
                cache.put(key, caffeineElement);
            }
        }
    }

    public void evictExpiredCaches() {
        Set<String> cacheNames = manager.keySet();
        for (String cacheName : cacheNames) {
            Cache<String, CaffeineElement> cache = manager.get(cacheName);
            cache.cleanUp();
        }
    }

    public long getCacheSize(String cacheName) {
        Cache<String, CaffeineElement> cache = getCacheWithName(cacheName);
        if (cache == null) {
            return 0;
        }
        return cache.estimatedSize();
    }

    public boolean isCacheEmpty(String cacheName) {
        if (getCacheWithName(cacheName) == null) {
            return true;
        }
        return getCacheWithName(cacheName).estimatedSize() == 0;
    }

    @Override
    public LocalCacheProvider getCacheProvider() {
        return LocalCacheProvider.CAFFEINE;
    }

    private Cache<Object, Object> getSpringCache(long timeToLive, long heapSize) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(timeToLive))
                .maximumSize(heapSize)
                .scheduler(Scheduler.systemScheduler());
        return builder.build();
    }

    @Override
    public CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs) {
        org.springframework.cache.caffeine.CaffeineCacheManager cacheManager = new org.springframework.cache.caffeine.CaffeineCacheManager();
        if (CollectionUtils.isNotEmpty(cacheConfigs)) {
            for (SpringCacheConfig cacheConfig : cacheConfigs) {
                cacheManager.registerCustomCache(cacheConfig.getCacheName(), getSpringCache(cacheConfig.getTimeToLive(), cacheConfig.getMaxSize()));
            }
        }
        return cacheManager;
    }
}