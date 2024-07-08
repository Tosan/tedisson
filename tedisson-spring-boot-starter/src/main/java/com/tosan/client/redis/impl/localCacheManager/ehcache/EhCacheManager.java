package com.tosan.client.redis.impl.localCacheManager.ehcache;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.SpringCacheConfig;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.enumuration.LocalCacheProvider;
import com.tosan.client.redis.exception.TedissonRuntimeException;
import com.tosan.client.redis.impl.localCacheManager.LocalCacheManagerBase;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheEventListenerConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.events.CacheEventListenerConfiguration;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.jcache.JCacheCacheManager;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 2/9/2022
 */
@Slf4j
public class EhCacheManager extends LocalCacheManagerBase implements LocalCacheManager {

    public static final String EHCACHE_JSR_107_CACHING_PROVIDER = "org.ehcache.jsr107.EhcacheCachingProvider";
    private final CacheManager manager;

    public EhCacheManager() {
        try {
            CachingProvider provider = Caching.getCachingProvider(EHCACHE_JSR_107_CACHING_PROVIDER);
            manager = provider.getCacheManager();
        } catch (CacheException e) {
            throw new TedissonRuntimeException("Can't create cache Manager!");
        }
    }

    @SuppressWarnings("rawtypes")
    public void createCache(String cacheName, CacheListener listener, CacheExpiryPolicy cacheExpiryPolicy, long heapSize) {
        if (!isCacheExist(cacheName)) {
            manager.createCache(cacheName, Eh107Configuration.fromEhcacheCacheConfiguration(getCacheConfiguration(
                    (CacheEventListener) listener, heapSize)));
            manager.enableManagement(cacheName, false);
            manager.enableStatistics(cacheName, false);
        }
    }

    @Override
    public void invalidateAllCacheItems(String cacheName) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("cache cleared : " + cacheName);
        } else {
            log.error("cache not found : " + cacheName);
        }
    }

    @Override
    public void updateItemExpiration(String cacheName, String key, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            EhCacheElement element = cache.get(key);
            if (element != null) {
                Date now = new Date();
                if (timeToLive != null) {
                    element.setExpirationTime(getItemExpirationTime(now, timeToLive, timeUnit));
                    element.setTimeToLiveSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
                } else {
                    element.setExpirationTime(null);
                    element.setTimeToLiveSecond(null);
                }
                if (timeToIdle != null) {
                    element.setMaxAllowedAccessTime(getItemExpirationTime(now, timeToIdle, timeUnit));
                    element.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToIdle, timeUnit));
                } else {
                    element.setMaxAllowedAccessTime(null);
                    element.setTimeToIdleSecond(null);
                }
                replaceCacheItem(cacheName, key, element.getValue());
            }
        }
    }

    public void removeCache(String cacheName) {
        manager.destroyCache(cacheName);
        super.removeCache(cacheName);
    }

    @SuppressWarnings("rawtypes")
    private CacheConfiguration<Object, Object> getCacheConfiguration(CacheEventListener listener, long heapSize) {
        CacheConfigurationBuilder<Object, Object> cacheConfigurationBuilder = getBasicCacheConfiguration(heapSize);
        if (listener != null) {
            CacheEventListenerConfiguration listenerConfiguration = CacheEventListenerConfigurationBuilder
                    .newEventListenerConfiguration(listener, getCacheEventTypes())
                    .asynchronous()
                    .unordered()
                    .build();
            cacheConfigurationBuilder = cacheConfigurationBuilder.withService(listenerConfiguration);
        }
        return cacheConfigurationBuilder.build();
    }

    private CacheConfigurationBuilder<Object, Object> getBasicCacheConfiguration(long heapSize) {
        return CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(heapSize));
    }

    public boolean isKeyInCache(String cacheName, String key) {
        if (key == null || cacheName == null || getCacheWithName(cacheName) == null) {
            return false;
        }
        return getItemFromCache(cacheName, key) != null;
    }

    private Cache<String, EhCacheElement> getCacheWithName(String cacheName) {
        return manager.getCache(cacheName);
    }

    public long incrementAndGetAtomicItem(String cacheName, String key) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            return (long) cache.invoke(key, new AddEntryProcessor()).getValue();
        }
        return 1;
    }

    public void resetAtomicItem(String cacheName, String key) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.invoke(key, new ResetEntryProcessor());
        }
    }

    @Override
    public void setAtomicItem(String cacheName, String key, long value) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.put(key, new EhCacheElement(value));
        }
    }

    @Override
    public void expireAtomicItem(String cacheName, String key, Long timeToLive, TimeUnit timeUnit) {
        CacheExpiryPolicy cacheExpiryPolicy = new CacheExpiryPolicy(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.invoke(key, new UpdateEntryExpirationProcessor(cacheExpiryPolicy));
        }
    }

    @Override
    public long getAtomicValue(String cacheName, String key) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            return (long) cache.invoke(key, new GetEntryProcessor()).getValue();
        }
        return 1;
    }

    public Set<String> getCacheKeySet(String cacheName) {
        javax.cache.Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache == null) {
            return null;
        }
        Set<String> keys = new HashSet<>();
        for (Cache.Entry<String, EhCacheElement> entry : cache) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    public void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, TimeUnit timeUnit) {
        addItemToCache(cacheName, key, value, timeToLive, null, timeUnit);
    }

    public void addItemToCache(String cacheName, Object key, Object value, Long timeToLive, Long timeToIdle, TimeUnit timeUnit) {
        EhCacheElement ehcacheElement = new EhCacheElement();
        Date now = new Date();
        if (timeToLive != null) {
            Date expirationTime = getItemExpirationTime(now, timeToLive, timeUnit);
            ehcacheElement.setExpirationTime(expirationTime);
            ehcacheElement.setTimeToLiveSecond(TimeUnit.SECONDS.convert(timeToLive, timeUnit));
        }
        if (timeToIdle != null) {
            Date maxAllowedAccessTime = getItemExpirationTime(now, timeToIdle, timeUnit);
            ehcacheElement.setMaxAllowedAccessTime(maxAllowedAccessTime);
            ehcacheElement.setTimeToIdleSecond(TimeUnit.SECONDS.convert(timeToIdle, timeUnit));
        }
        ehcacheElement.setValue(value);
        manager.getCache(cacheName).put(key, ehcacheElement);
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
        javax.cache.Cache<String, EhCacheElement> cache = manager.getCache(cacheName);
        if (cache != null) {
            List<T> items = new ArrayList<>();
            for (Cache.Entry<String, EhCacheElement> entry : cache) {
                if (getItemFromCache(cacheName, entry.getKey()) != null) {
                    if (entry.getValue().getValue()!=null) {
                        items.add((T) entry.getValue().getValue());
                    }
                }
            }
            return items;
        }
        return null;
    }

    public void addAllToCache(String cacheName, Map<String, Object> items, Long timeToLive, TimeUnit timeUnit) {
        if (MapUtils.isNotEmpty(items)) {
            for (Map.Entry<String, Object> entry : items.entrySet()) {
                addItemToCache(cacheName, entry.getKey(), entry.getValue(), timeToLive, null, timeUnit);
            }
        }
    }

    private Date getItemExpirationTime(Date now, Long duration, TimeUnit timeUnit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, (int) TimeUnit.SECONDS.convert(duration, timeUnit));
        return calendar.getTime();
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
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        EhCacheElement ehcacheElement = null;
        if (cache != null) {
            ehcacheElement = cache.get(key);
            if (ehcacheElement == null) {
                return null;
            }
            Date now = new Date();
            if (isCacheItemExpired(ehcacheElement, now)) {
                ehcacheElement.setExpirationTime(now);
                cache.remove(key);
                return null;
            }
            if (ehcacheElement.getTimeToIdleSecond() != null) {
                ehcacheElement.setMaxAllowedAccessTime(getItemExpirationTime(now, ehcacheElement.getTimeToIdleSecond(), TimeUnit.SECONDS));
            }
        }
        if (ehcacheElement != null && ehcacheElement.getValue() != null) {
            return (T) ehcacheElement.getValue();
        }
        return null;
    }

    private boolean isCacheItemExpired(EhCacheElement ehcacheElement, Date now) {
        if (ehcacheElement.getExpirationTime() != null && ehcacheElement.getExpirationTime().before(now)) {
            return true;
        }
        return ehcacheElement.getMaxAllowedAccessTime() != null && ehcacheElement.getMaxAllowedAccessTime().before(now);
    }

    public void removeItemFromCache(String cacheName, String key) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            cache.remove(key);
        }
    }

    public void replaceCacheItem(String cacheName, String key, Object value) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache != null) {
            EhCacheElement oldEhCacheElement = cache.get(key);
            if (oldEhCacheElement != null) {
                EhCacheElement ehcacheElement = new EhCacheElement(oldEhCacheElement.getTimeToLiveSecond(), oldEhCacheElement.getTimeToIdleSecond(),
                        oldEhCacheElement.getExpirationTime(), oldEhCacheElement.getMaxAllowedAccessTime(), value);
                cache.replace(key, ehcacheElement);
            }
        }
    }

    public void evictExpiredCaches() {
        Iterable<String> cacheNames = manager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache<Object, Object> cache = manager.getCache(cacheName);
            cache.iterator();
        }
    }

    public long getCacheSize(String cacheName) {
        Cache<String, EhCacheElement> cache = getCacheWithName(cacheName);
        if (cache == null) {
            return 0;
        }
        long size = 0;
        for (Cache.Entry<String, EhCacheElement> ignored : cache) {
            size++;
        }
        return size;
    }

    public boolean isCacheEmpty(String cacheName) {
        if (getCacheWithName(cacheName) == null) {
            return true;
        }
        getAllFromCache(cacheName);
        return !getCacheWithName(cacheName).iterator().hasNext();
    }

    @Override
    public LocalCacheProvider getCacheProvider() {
        return LocalCacheProvider.EHCACHE;
    }

    private CacheConfiguration<Object, Object> getSpringCacheConfiguration(long timeToLive, long heapSize) {
        CacheConfigurationBuilder<Object, Object> cacheConfigurationBuilder = getBasicCacheConfiguration(heapSize);
        return cacheConfigurationBuilder.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(timeToLive))).build();
    }

    @Override
    public org.springframework.cache.CacheManager getSpringCacheManager(List<SpringCacheConfig> cacheConfigs) {
        CacheManager cacheManager = Caching.getCachingProvider(EHCACHE_JSR_107_CACHING_PROVIDER).getCacheManager();
        if (CollectionUtils.isNotEmpty(cacheConfigs)) {
            for (SpringCacheConfig cacheConfig : cacheConfigs) {
                cacheManager.createCache(cacheConfig.getCacheName(), Eh107Configuration.fromEhcacheCacheConfiguration(getSpringCacheConfiguration(
                        cacheConfig.getTimeToLive(), cacheConfig.getMaxSize())));
            }
        }
        return new JCacheCacheManager(cacheManager);
    }

    private Set<EventType> getCacheEventTypes() {
        return new HashSet<>(Arrays.asList(EventType.values()));
    }
}
