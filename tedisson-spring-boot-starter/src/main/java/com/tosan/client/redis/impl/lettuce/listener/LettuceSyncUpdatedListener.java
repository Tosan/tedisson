package com.tosan.client.redis.impl.lettuce.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.LettuceListener;
import com.tosan.client.redis.enumuration.LettuceListenerEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * Lettuce listener for cache updated events using Redis pub/sub
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@Slf4j
public class LettuceSyncUpdatedListener implements LettuceListener {

    private final LocalCacheManager localCacheManager;

    public LettuceSyncUpdatedListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onMessage(String cacheName, String key, Object value, LettuceListenerEventType eventType) {
        if (value != null && eventType.equals(LettuceListenerEventType.UPDATED)) {
            localCacheManager.replaceCacheItem(cacheName, key, value);
            log.debug("Cache item updated and synced to local: {} - {}", cacheName, key);
        }
    }
}