package com.tosan.client.redis.scheduler;

import com.tosan.client.redis.configuration.stream.StreamProperties;
import com.tosan.client.redis.stream.MessageParameter;
import com.tosan.client.redis.stream.MessageQueueManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 1/14/2023
 */
@Slf4j
public class TedissonStreamScheduler {
    private final MessageQueueManager messageQueueManager;
    private final StreamProperties streamProperties;

    public TedissonStreamScheduler(@Lazy MessageQueueManager messageQueueManager, StreamProperties streamProperties) {
        this.messageQueueManager = messageQueueManager;
        this.streamProperties = streamProperties;
    }

    @Scheduled(fixedRateString = "${tedisson.redis.stream.trim-rate-second:3600}", timeUnit = TimeUnit.SECONDS,
            initialDelayString = "#{ T(java.util.concurrent.ThreadLocalRandom).current().nextInt(30) }")
    public void trimStream() {
        Long trimmedSize = messageQueueManager.trim(MessageParameter.TEDISSON_STREAM_NAME, streamProperties.getMaxMessageSize());
        log.debug("Stream with name {} trimmed. trim size: {}", MessageParameter.TEDISSON_STREAM_NAME, trimmedSize);
    }
}
