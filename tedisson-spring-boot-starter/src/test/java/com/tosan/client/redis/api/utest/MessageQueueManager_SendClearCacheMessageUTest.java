package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.configuration.stream.StreamProperties;
import com.tosan.client.redis.stream.MessageParameter;
import com.tosan.client.redis.stream.MessageQueueManager;
import com.tosan.client.redis.stream.type.StreamMessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;

import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * @author R.Mehri
 * @since 6/20/2023
 */
@ExtendWith(MockitoExtension.class)
public class MessageQueueManager_SendClearCacheMessageUTest {

    private static final String CACHE_NAME = "CACHE";
    private MessageQueueManager messageQueueManager;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamProperties streamProperties;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @BeforeEach
    public void setup() {
        messageQueueManager = new MessageQueueManager(redisTemplate, streamProperties);
    }

    @Test
    public void sendCorrectMessageWhenStreamIsEnabled() {
        when(streamProperties.isEnabled()).thenReturn(true);
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        messageQueueManager.sendClearCacheMessage(CACHE_NAME);
        HashMap<String, String> messageParam = new HashMap<>();
        messageParam.put(StreamMessageType.CLEAR_CACHE.name(), CACHE_NAME);
        verify(streamOperations).add(MessageParameter.TEDISSON_STREAM_NAME, messageParam);
    }

    @Test
    public void dontSendMessageWhenStreamIsNotEnabled() {
        when(streamProperties.isEnabled()).thenReturn(false);
        messageQueueManager.sendClearCacheMessage(CACHE_NAME);
        HashMap<String, String> messageParam = new HashMap<>();
        messageParam.put(StreamMessageType.CLEAR_CACHE.name(), CACHE_NAME);
        verify(streamOperations, times(0)).add(MessageParameter.TEDISSON_STREAM_NAME, messageParam);
    }
}
