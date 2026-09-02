package com.tosan.client.redis.impl.lettuce.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.LettuceListener;
import com.tosan.client.redis.enumuration.LettuceListenerEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * Lettuce listener for cache removed events using Redis pub/sub
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@Slf4j
public class LettuceSyncRemovedListener implements LettuceListener {

    private final LocalCacheManager localCacheManager;

    public LettuceSyncRemovedListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onMessage(String cacheName, String key, Object value, LettuceListenerEventType eventType) {
        if (value != null && eventType.equals(LettuceListenerEventType.REMOVED)) {
            localCacheManager.removeItemFromCache(cacheName, key);
            log.debug("Cache item removed and synced to local: {} - {}", cacheName, key);
        }
    }
}