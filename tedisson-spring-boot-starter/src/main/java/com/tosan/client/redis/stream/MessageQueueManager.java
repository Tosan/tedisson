package com.tosan.client.redis.stream;

import com.tosan.client.redis.configuration.stream.StreamProperties;
import com.tosan.client.redis.stream.type.StreamMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author R.Mehri
 * @since 6/14/2021
 */
@Slf4j
public class MessageQueueManager {
    private final RedisTemplate<String, String> redisTemplate;
    private final StreamProperties streamProperties;

    public MessageQueueManager(@Lazy RedisTemplate<String, String> redisTemplate, StreamProperties streamProperties) {
        this.redisTemplate = redisTemplate;
        this.streamProperties = streamProperties;
    }

    public void sendClearCacheMessage(String cacheName) {
        if (streamProperties.isEnabled()) {
            HashMap<String, String> messageParam = new HashMap<>();
            messageParam.put(StreamMessageType.CLEAR_CACHE.name(), cacheName);
            redisTemplate.opsForStream().add(MessageParameter.TEDISSON_STREAM_NAME, messageParam);
            if (log.isDebugEnabled()) {
                for (Map.Entry<String, String> message : messageParam.entrySet()) {
                    log.debug("message sent with key: " + message.getKey() + " and value: " + message.getValue());
                }
            }
        }
    }

    public Long trim(String streamName, long maxCount) {
        return redisTemplate.opsForStream().trim(streamName, maxCount);
    }
}
