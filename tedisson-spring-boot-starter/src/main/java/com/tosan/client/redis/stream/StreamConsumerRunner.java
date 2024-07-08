package com.tosan.client.redis.stream;

import com.tosan.client.redis.stream.type.StreamMessageListenerConsumerErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author R.Mehri
 * @since 6/14/2021
 */
@Slf4j
public class StreamConsumerRunner implements ApplicationRunner, DisposableBean {
    private final RedisConnectionFactory redisConnectionFactory;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final ConsumerListener consumerListener;
    private final RedisTemplate<String, String> redisTemplate;
    private final int errorLogIntervalInSecond = 60;
    private final Map<StreamMessageListenerConsumerErrorType, Date> lastErrorLogTimeMap = new HashMap<>();
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer;
    private Subscription registeredSubscription;

    public StreamConsumerRunner(RedisConnectionFactory redisConnectionFactory, ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                ConsumerListener consumerListener, RedisTemplate<String, String> redisTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.consumerListener = consumerListener;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeStreamMessageListenerContainer();
    }

    private void initializeStreamMessageListenerContainer() {
        this.init();
        redisTemplate.opsForStream().createGroup(MessageParameter.TEDISSON_STREAM_NAME, MessageParameter.TEDISSON_GROUP_NAME);
        StreamMessageListenerContainer.ConsumerStreamReadRequest<String> readOptions =
                StreamMessageListenerContainer.StreamReadRequest
                        .builder(StreamOffset.create(MessageParameter.TEDISSON_STREAM_NAME, ReadOffset.lastConsumed()))
                        .cancelOnError((ex) -> false)
                        .consumer(Consumer.from(MessageParameter.TEDISSON_GROUP_NAME, MessageParameter.TEDISSON_STREAM_NAME))
                        .build();
        if (registeredSubscription != null) {
            streamMessageListenerContainer.remove(registeredSubscription);
        }
        registeredSubscription = streamMessageListenerContainer.register(readOptions, consumerListener);
        this.streamMessageListenerContainer.start();
    }

    private void reinitializeStreamMessageListenerContainer() {
        this.streamMessageListenerContainer.stop();
        initializeStreamMessageListenerContainer();
    }

    @Override
    public void destroy() {
        this.streamMessageListenerContainer.stop();
    }

    private void init() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .executor(this.threadPoolTaskExecutor)
                        .errorHandler(this::streamMessageListenerErrorHandler)
                        .pollTimeout(Duration.ZERO)
                        .serializer(new StringRedisSerializer())
                        .build();

        this.streamMessageListenerContainer = StreamMessageListenerContainer
                .create(this.redisConnectionFactory, streamMessageListenerContainerOptions);
    }

    private void streamMessageListenerErrorHandler(Throwable throwable) {
        //todo: complete error handling business for another error type that extract from getErrorType method.
        StreamMessageListenerConsumerErrorType errorType = getListenerConsumerErrorType(throwable);
        switch (errorType) {
            case NO_GROUP:
                log.error("StreamMessageListenerContainerErrorHandler: {}", throwable.getMessage(), throwable);
                log.info("reinitialization of StreamMessageListenerContainer is started.");
                reinitializeStreamMessageListenerContainer();
                log.info("reinitialization of StreamMessageListenerContainer is finished.");
                break;
            case NO_REPLICA:
                if (lastErrorLogTimeMap.get(errorType) == null ||
                        ((new Date().getTime() - lastErrorLogTimeMap
                                .get(errorType).getTime()) / 1000) > errorLogIntervalInSecond) {
                    log.error("StreamMessageListenerContainerErrorHandler: {}", throwable.getMessage(), throwable);
                    lastErrorLogTimeMap.put(errorType, new Date());
                }
                break;
            case UNKNOWN:
            case OTHER:
            default:
                log.error("StreamMessageListenerContainerErrorHandler: {}", throwable.getMessage(), throwable);
        }
    }

    public StreamMessageListenerConsumerErrorType getListenerConsumerErrorType(Throwable throwable) {
        String message = throwable.getMessage();
        if (message != null) {
            if (message.startsWith("NOGROUP")) {
                return StreamMessageListenerConsumerErrorType.NO_GROUP;
            } else if (message.startsWith("NOREPLICAS")) {
                return StreamMessageListenerConsumerErrorType.NO_REPLICA;
            } else {
                return StreamMessageListenerConsumerErrorType.OTHER;
            }
        } else {
            return StreamMessageListenerConsumerErrorType.UNKNOWN;
        }
    }
}
