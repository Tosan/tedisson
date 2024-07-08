package com.tosan.client.redis.configuration;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.configuration.redisson.TedissonProperties;
import com.tosan.client.redis.exception.TedissonException;
import com.tosan.client.redis.impl.TedissonLocalCacheManagerImpl;
import com.tosan.client.redis.impl.localCacheManager.LocalCacheManagerBase;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineCacheManager;
import com.tosan.client.redis.impl.localCacheManager.ehcache.EhCacheManager;
import com.tosan.client.redis.impl.redisson.TedissonCentralCacheManagerImpl;
import com.tosan.client.redis.impl.redisson.listener.TedissonCreatedSyncListener;
import com.tosan.client.redis.impl.redisson.listener.TedissonRemovedSyncListener;
import com.tosan.client.redis.impl.redisson.listener.TedissonUpdatedSyncListener;
import com.tosan.client.redis.scheduler.TedissonStreamScheduler;
import com.tosan.client.redis.stream.ConsumerListener;
import com.tosan.client.redis.stream.MessageQueueManager;
import com.tosan.client.redis.stream.StreamConsumerRunner;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author R.Mehri
 * @since 12/26/2022
 */
@Configuration
@EnableConfigurationProperties({TedissonProperties.class})
public class TedissonAutoConfiguration {

    @Autowired
    private TedissonProperties tedissonProperties;

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "false", matchIfMissing = true)
    public TedissonCacheManager tedissonLocalCacheManager(LocalCacheManager localCacheManager) {
        return new TedissonLocalCacheManagerImpl(localCacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "true")
    public TedissonCacheManager tedissonCentralCacheManager(RedissonClient redissonClient,
                                                            LocalCacheManager localCacheManager,
                                                            TedissonCreatedSyncListener createdSyncListener,
                                                            TedissonRemovedSyncListener removedSyncListener,
                                                            TedissonUpdatedSyncListener updatedSyncListener,
                                                            Optional<MessageQueueManager> messageQueueManager) {
        TedissonCentralCacheManagerImpl centralCacheManager = new TedissonCentralCacheManagerImpl(redissonClient);
        centralCacheManager.setLocalCacheManager(localCacheManager);
        centralCacheManager.setCreatedSyncListener(createdSyncListener);
        centralCacheManager.setRemovedSyncListener(removedSyncListener);
        centralCacheManager.setUpdatedSyncListener(updatedSyncListener);
        messageQueueManager.ifPresent(centralCacheManager::setMessageQueueManager);
        if (tedissonProperties != null && tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            centralCacheManager.setMessageQueueEnable(tedissonProperties.getRedis().getStream().isEnabled());
        }
        return centralCacheManager;
    }

    @Bean("localCacheManager")
    @Primary
    @ConditionalOnProperty(name = "tedisson.local.cache-provider", havingValue = "ehcache", matchIfMissing = true)
    public EhCacheManager ehcacheLocalCacheManager(Optional<MessageQueueManager> messageQueueManager) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        if (tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            messageQueueManager.ifPresent(ehCacheManager::setMessageQueueManager);
        }
        return ehCacheManager;
    }

    @Bean("localCacheManager")
    @ConditionalOnProperty(name = "tedisson.local.cache-provider", havingValue = "caffeine", matchIfMissing = true)
    public CaffeineCacheManager caffeineLocalCacheManager(Optional<MessageQueueManager> messageQueueManager) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        if (tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            messageQueueManager.ifPresent(caffeineCacheManager::setMessageQueueManager);
        }
        return caffeineCacheManager;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "true")
    public RedissonClient redissonClient() throws TedissonException {
        return new RedissonClientFactory(tedissonProperties).getInstance();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    @ConditionalOnProperty(name = "tedisson.redis.stream.enabled", havingValue = "true")
    public RedissonClient streamRedissonClient() throws TedissonException {
        return new RedissonClientFactory(tedissonProperties).getInstance();
    }

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "true")
    TedissonCreatedSyncListener createdSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonCreatedSyncListener(localCacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "true")
    TedissonUpdatedSyncListener updatedSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonUpdatedSyncListener(localCacheManager);
    }

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "true")
    TedissonRemovedSyncListener removedSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonRemovedSyncListener(localCacheManager);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        StringRedisSerializer redisSerializer = new StringRedisSerializer();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(redisSerializer);
        template.setHashKeySerializer(redisSerializer);
        template.setHashValueSerializer(redisSerializer);
        template.setValueSerializer(redisSerializer);
        return template;
    }

    @Bean
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    @Bean(name = "tedissonThreadPoolTaskExecutor", destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(tedissonProperties.getRedis().getStream().getThreadPool().getCorePoolSize());
        executor.setMaxPoolSize(tedissonProperties.getRedis().getStream().getThreadPool().getMaxPoolSize());
        executor.setQueueCapacity(tedissonProperties.getRedis().getStream().getThreadPool().getQueueCapacity());
        executor.setThreadNamePrefix("redisStream-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public MessageQueueManager messageQueueManager(RedisTemplate<String, String> redisTemplate) {
        return new MessageQueueManager(redisTemplate, tedissonProperties.getRedis().getStream());
    }

    @Bean
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public TedissonStreamScheduler scheduledTasks(MessageQueueManager messageQueueManager) {
        return new TedissonStreamScheduler(messageQueueManager, tedissonProperties.getRedis().getStream());
    }

    @Bean("tedissonConsumerListener")
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public ConsumerListener consumerListener(LocalCacheManagerBase localCacheManager, RedisTemplate<String, String> redisTemplate) {
        return new ConsumerListener(localCacheManager, redisTemplate);
    }

    @Bean("TedissonStreamConsumerRunner")
    @ConditionalOnProperty(prefix = "tedisson.redis.stream", name = "enabled", havingValue = "true")
    public StreamConsumerRunner streamConsumerRunner(RedisConnectionFactory connectionFactory,
                                                     @Qualifier("tedissonThreadPoolTaskExecutor") ThreadPoolTaskExecutor taskExecutor,
                                                     @Qualifier("tedissonConsumerListener") ConsumerListener listener,
                                                     RedisTemplate<String, String> redisTemplate) {
        return new StreamConsumerRunner(connectionFactory, taskExecutor, listener, redisTemplate);
    }

    @Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean
    public ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor() {
        return new ScheduledAnnotationBeanPostProcessor();
    }
}
