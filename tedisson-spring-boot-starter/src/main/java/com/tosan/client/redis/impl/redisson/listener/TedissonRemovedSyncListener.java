package com.tosan.client.redis.impl.redisson.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.impl.redisson.CacheElement;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryRemovedListener;

/**
 * @author R.Mehri
 * @since 11/22/2022
 */
public class TedissonRemovedSyncListener implements EntryRemovedListener<String, CacheElement>, CacheListener {
    private final LocalCacheManager localCacheManager;

    public TedissonRemovedSyncListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onRemoved(EntryEvent<String, CacheElement> event) {
        String cacheName = event.getSource().getName();
        if (cacheName != null) {
            localCacheManager.removeItemFromCache(cacheName, event.getKey());
        }
    }
}
