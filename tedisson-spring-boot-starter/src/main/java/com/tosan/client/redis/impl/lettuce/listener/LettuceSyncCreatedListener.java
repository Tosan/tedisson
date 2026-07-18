package com.tosan.client.redis.impl.lettuce.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.impl.lettuce.LettuceCacheElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Lettuce listener for cache created events using Redis pub/sub
 *
 * @author R.Mehri
 * @since 5/24/2026
 */
@Slf4j
public class LettuceSyncCreatedListener implements MessageListener, CacheListener {

    private final LocalCacheManager localCacheManager;
    private final JsonMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    public LettuceSyncCreatedListener(LocalCacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            String channel = new String(message.getChannel());

            if (channel != null && channel.startsWith("cache:created:")) {
                String[] parts = channel.split(":");
                if (parts.length >= 4) {
                    String cacheName = parts[2];
                    String key = parts[3];
                    // Extract value from payload
                    LettuceCacheElement element = objectMapper.readValue(payload, LettuceCacheElement.class);
                    if (element != null && !localCacheManager.isKeyInCache(cacheName, key)) {
                        localCacheManager.addItemToCache(cacheName, key, element.getData());
                        log.debug("Cache item created and synced to local: {} - {}", cacheName, key);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error processing cache created message", e);
        }
    }
}
