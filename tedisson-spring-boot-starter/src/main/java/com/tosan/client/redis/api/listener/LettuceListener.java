package com.tosan.client.redis.api.listener;

import com.tosan.client.redis.configuration.serializer.Kryo5RedisSerializer;
import com.tosan.client.redis.enumuration.LettuceListenerEventType;
import com.tosan.client.redis.impl.lettuce.LettuceCacheElement;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;

public interface LettuceListener extends MessageListener, CacheListener {

    Kryo5RedisSerializer<LettuceCacheElement> serializer = new Kryo5RedisSerializer<>();

    @Override
    default void onMessage(Message message, @Nullable byte[] pattern) {
        String channel = new String(message.getChannel());

        LettuceCacheElement element = null;
        String cacheName = null;
        String key = null;
        LettuceListenerEventType eventType = null;
        if (channel != null) {
            element = serializer.deserialize(message.getBody());
            String[] parts = channel.split(":");
            if (parts.length >= 4) {
                cacheName = parts[2];
                key = parts[3];
                // Extract value from payload
            }
            if (channel.startsWith("cache:created:")) {
                eventType = LettuceListenerEventType.CREATED;
            } else if (channel.startsWith("cache:updated:")) {
                eventType = LettuceListenerEventType.UPDATED;
            } else if (channel.startsWith("cache:deleted:")) {
                eventType = LettuceListenerEventType.REMOVED;
            }
            onMessage(cacheName, key, element.getData(), eventType);
        }
    }

    void onMessage(String cacheName, String key, Object value, LettuceListenerEventType eventType);
}