package com.tosan.client.redis.impl.redisson.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.impl.redisson.CacheElement;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;

/**
 * @author R.Mehri
 * @since 12/3/2022
 */
public class TedissonCreatedSyncListener implements EntryCreatedListener<String, CacheElement>, CacheListener {
    private final LocalCacheManager localCacheManager;

    public TedissonCreatedSyncListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onCreated(EntryEvent<String, CacheElement> event) {
        String cacheName = event.getSource().getName();
        if (cacheName != null && !localCacheManager.isKeyInCache(cacheName, event.getKey())) {
            localCacheManager.addItemToCache(cacheName, event.getKey(), event.getValue().getData());
        }
    }
}
