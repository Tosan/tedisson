package com.tosan.client.redis.impl.redisson.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.impl.redisson.CacheElement;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryUpdatedListener;

/**
 * @author R.Mehri
 * @since 11/19/2022
 */
public class TedissonUpdatedSyncListener implements EntryUpdatedListener<String, CacheElement>, CacheListener {

    private final LocalCacheManager localCacheManager;

    public TedissonUpdatedSyncListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onUpdated(EntryEvent<String, CacheElement> event) {
        String cacheName = event.getSource().getName();
        if (cacheName != null) {
            localCacheManager.replaceCacheItem(cacheName, event.getKey(), event.getValue().getData());
        }
    }
}
