package com.tosan.client.redis.impl.lettuce.listener;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Lettuce listener for cache removed events using Redis pub/sub
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@Slf4j
public class LettuceSyncRemovedListener implements MessageListener, CacheListener {
    
    private final LocalCacheManager localCacheManager;
    
    public LettuceSyncRemovedListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            
            if (channel != null && channel.startsWith("cache:removed:")) {
                String[] parts = channel.split(":");
                if (parts.length >= 4) {
                    String cacheName = parts[2];
                    String key = parts[3];
                    
                    localCacheManager.removeItemFromCache(cacheName, key);
                    log.debug("Cache item removed and synced to local: {} - {}", cacheName, key);
                }
            }
        } catch (Exception e) {
            log.warn("Error processing cache removed message", e);
        }
    }
}