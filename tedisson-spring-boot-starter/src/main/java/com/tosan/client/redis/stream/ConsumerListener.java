package com.tosan.client.redis.stream;

import com.tosan.client.redis.impl.localCacheManager.LocalCacheManagerBase;
import com.tosan.client.redis.stream.type.StreamMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;

import java.util.Map;

/**
 * @author R.Mehri
 * @since 6/15/2021
 */
@Slf4j
public class ConsumerListener implements StreamListener<String, MapRecord<String, String, String>> {
    private final LocalCacheManagerBase localCacheManager;
    private final RedisTemplate<String, String> redisTemplate;

    public ConsumerListener(LocalCacheManagerBase localCacheManager, @Lazy RedisTemplate<String, String> redisTemplate) {
        this.localCacheManager = localCacheManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> map = message.getValue();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            handleMessage(entry.getKey(), entry.getValue());
        }
        redisTemplate.opsForStream().acknowledge(
                MessageParameter.TEDISSON_STREAM_NAME, MessageParameter.TEDISSON_GROUP_NAME, message.getId());
    }

    private void handleMessage(String messageType, Object parameter) {
        StreamMessageType streamMessageType = StreamMessageType.valueOf(messageType);
        if (messageType != null) {
            if (streamMessageType == StreamMessageType.CLEAR_CACHE) {
                handleClearCacheMessage(String.valueOf(parameter));
            }
        }
    }

    private void handleClearCacheMessage(String cacheName) {
        try {
            localCacheManager.invalidateAllCacheItems(cacheName);
        } catch (Exception ex) {
            log.error("there is no cacheName called " + cacheName);
        }
    }
}
