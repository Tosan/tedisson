package com.tosan.client.redis.impl.lettuce.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.LettuceListener;
import com.tosan.client.redis.enumuration.LettuceListenerEventType;
import lombok.extern.slf4j.Slf4j;

/**
 * Lettuce listener for cache created events using Redis pub/sub
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@Slf4j
public class LettuceSyncCreatedListener implements LettuceListener {

    private final LocalCacheManager localCacheManager;

    public LettuceSyncCreatedListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onMessage(String cacheName, String key, Object value, LettuceListenerEventType eventType) {
        if (value != null && eventType.equals(LettuceListenerEventType.CREATED)) {
            localCacheManager.addItemToCache(cacheName, key, value);
            log.debug("Cache item created and synced to local: {} - {}", cacheName, key);
        }
    }
}