package com.tosan.client.redis.configuration;

import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.configuration.condition.OnLettuceEnabledCondition;
import com.tosan.client.redis.configuration.condition.OnRedissonEnabledCondition;
import com.tosan.client.redis.configuration.condition.OnRedissonStreamEnabledCondition;
import com.tosan.client.redis.configuration.redisson.TedissonProperties;
import com.tosan.client.redis.configuration.redisson.TedissonPropertiesCustomizer;
import com.tosan.client.redis.configuration.serializer.Kryo5RedisSerializer;
import com.tosan.client.redis.exception.TedissonException;
import com.tosan.client.redis.impl.TedissonLocalCacheManagerImpl;
import com.tosan.client.redis.impl.lettuce.LettuceCacheElement;
import com.tosan.client.redis.impl.lettuce.TedissonLettuceCacheManagerImpl;
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncCreatedListener;
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncRemovedListener;
import com.tosan.client.redis.impl.lettuce.listener.LettuceSyncUpdatedListener;
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
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author R.Mehri
 * @since 12/26/2022
 */
@Configuration
@EnableConfigurationProperties({TedissonProperties.class, RedisProperties.class})
public class TedissonAutoConfiguration {

    @Autowired
    private TedissonProperties tedissonProperties;
    @Autowired(required = false)
    private List<TedissonPropertiesCustomizer> customizerList;

    // -------------------------------------------------------------------------
    // Cache manager — local only (redis disabled)
    // -------------------------------------------------------------------------

    @Bean
    @ConditionalOnProperty(name = "tedisson.redis.enabled", havingValue = "false", matchIfMissing = true)
    public TedissonCacheManager tedissonLocalCacheManager(LocalCacheManager localCacheManager) {
        return new TedissonLocalCacheManagerImpl(localCacheManager);
    }

    // -------------------------------------------------------------------------
    // Cache manager — Redisson (default central client)
    // Connection configured via tedisson.redis.* properties
    // -------------------------------------------------------------------------

    @Bean
    @Conditional(OnRedissonEnabledCondition.class)
    public TedissonCacheManager tedissonCentralRedissonCacheManager(
            LocalCacheManager localCacheManager,
            RedissonClient redissonClient,
            Optional<TedissonCreatedSyncListener> createdSyncListener,
            Optional<TedissonRemovedSyncListener> removedSyncListener,
            Optional<TedissonUpdatedSyncListener> updatedSyncListener,
            Optional<MessageQueueManager> messageQueueManager) {
        TedissonCentralCacheManagerImpl centralCacheManager = new TedissonCentralCacheManagerImpl(redissonClient);
        centralCacheManager.setLocalCacheManager(localCacheManager);
        createdSyncListener.ifPresent(centralCacheManager::setCreatedSyncListener);
        removedSyncListener.ifPresent(centralCacheManager::setRemovedSyncListener);
        updatedSyncListener.ifPresent(centralCacheManager::setUpdatedSyncListener);
        messageQueueManager.ifPresent(centralCacheManager::setMessageQueueManager);
        if (tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            centralCacheManager.setMessageQueueEnable(tedissonProperties.getRedis().getStream().isEnabled());
        }
        return centralCacheManager;
    }

    // -------------------------------------------------------------------------
    // Cache manager — Lettuce
    // Connection provided by the main application via spring.data.redis.* properties.
    // The starter does NOT create its own RedisConnectionFactory for Lettuce.
    // -------------------------------------------------------------------------

    @Bean
    @Conditional(OnLettuceEnabledCondition.class)
    public TedissonCacheManager tedissonCentralLettuceCacheManager(
            LocalCacheManager localCacheManager,
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<LettuceCacheElement> redisSerializer,
            Optional<LettuceSyncCreatedListener> lettuceCreatedListener,
            Optional<LettuceSyncRemovedListener> lettuceRemovedListener,
            Optional<LettuceSyncUpdatedListener> lettuceUpdatedListener,
            Optional<MessageQueueManager> messageQueueManager) {
        TedissonLettuceCacheManagerImpl lettuceCacheManager = new TedissonLettuceCacheManagerImpl(redisConnectionFactory, redisSerializer);
        lettuceCacheManager.setLocalCacheManager(localCacheManager);
        lettuceCreatedListener.ifPresent(lettuceCacheManager::setCreatedSyncListener);
        lettuceRemovedListener.ifPresent(lettuceCacheManager::setRemovedSyncListener);
        lettuceUpdatedListener.ifPresent(lettuceCacheManager::setUpdatedSyncListener);
        messageQueueManager.ifPresent(lettuceCacheManager::setMessageQueueManager);
        if (tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            lettuceCacheManager.setMessageQueueEnable(tedissonProperties.getRedis().getStream().isEnabled());
        }
        return lettuceCacheManager;
    }

    // -------------------------------------------------------------------------
    // Local cache providers
    // -------------------------------------------------------------------------

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
    @ConditionalOnProperty(name = "tedisson.local.cache-provider", havingValue = "caffeine")
    public CaffeineCacheManager caffeineLocalCacheManager(Optional<MessageQueueManager> messageQueueManager) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        if (tedissonProperties.getRedis() != null && tedissonProperties.getRedis().getStream() != null) {
            messageQueueManager.ifPresent(caffeineCacheManager::setMessageQueueManager);
        }
        return caffeineCacheManager;
    }

    // -------------------------------------------------------------------------
    // Redisson client — only when client type is REDISSON
    // -------------------------------------------------------------------------

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    @Conditional(OnRedissonEnabledCondition.class)
    public RedissonClient redissonClient() throws TedissonException {
        return new RedissonClientFactory(tedissonProperties, customizerList).getInstance();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    @Conditional(OnRedissonStreamEnabledCondition.class)
    public RedissonClient streamRedissonClient() throws TedissonException {
        return new RedissonClientFactory(tedissonProperties, customizerList).getInstance();
    }

    // -------------------------------------------------------------------------
    // Redisson listeners — only when client type is REDISSON
    // -------------------------------------------------------------------------

    @Bean
    @Conditional(OnRedissonEnabledCondition.class)
    TedissonCreatedSyncListener createdSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonCreatedSyncListener(localCacheManager);
    }

    @Bean
    @Conditional(OnRedissonEnabledCondition.class)
    TedissonUpdatedSyncListener updatedSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonUpdatedSyncListener(localCacheManager);
    }

    @Bean
    @Conditional(OnRedissonEnabledCondition.class)
    TedissonRemovedSyncListener removedSyncListener(LocalCacheManager localCacheManager) {
        return new TedissonRemovedSyncListener(localCacheManager);
    }

    // -------------------------------------------------------------------------
    // Lettuce listeners — only when client type is LETTUCE
    // -------------------------------------------------------------------------

    @Bean
    @Conditional(OnLettuceEnabledCondition.class)
    LettuceSyncCreatedListener lettuceSyncCreatedListener(LocalCacheManager localCacheManager, RedisSerializer<LettuceCacheElement> redisSerializer) {
        return new LettuceSyncCreatedListener(localCacheManager);
    }

    @Bean
    @Conditional(OnLettuceEnabledCondition.class)
    LettuceSyncUpdatedListener lettuceSyncUpdatedListener(LocalCacheManager localCacheManager, RedisSerializer<LettuceCacheElement> redisSerializer) {
        return new LettuceSyncUpdatedListener(localCacheManager);
    }

    @Bean
    @Conditional(OnLettuceEnabledCondition.class)
    LettuceSyncRemovedListener lettuceSyncRemovedListener(LocalCacheManager localCacheManager, RedisSerializer<LettuceCacheElement> redisSerializer) {
        return new LettuceSyncRemovedListener(localCacheManager);
    }

    // -------------------------------------------------------------------------
    // Lettuce kryo5 serializer — only when client type is LETTUCE
    // -----

    @Bean
    @Conditional(OnLettuceEnabledCondition.class)
    RedisSerializer<LettuceCacheElement> redisSerializer() {
        return new Kryo5RedisSerializer<>();
    }

    // -------------------------------------------------------------------------
    // Stream support
    // For REDISSON: wraps RedissonClient into a RedisConnectionFactory so
    //               Spring Boot's LettuceConnectionFactory is not created.
    // For LETTUCE:  redissonConnectionFactory is skipped; the stream template
    //               uses Spring Boot's RedisConnectionFactory directly.
    // -------------------------------------------------------------------------

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
    @Conditional(OnRedissonStreamEnabledCondition.class)
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redissonClient) {
        return new RedissonConnectionFactory(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(LettuceConnectionFactory.class)
    public LettuceConnectionFactory lettuceConnectionFactory(RedisProperties properties) {
        LettuceClientConfiguration clientConfiguration = lettuceClientConfiguration(properties);
        // Cluster
        if (properties.getCluster() != null
                && properties.getCluster().getNodes() != null
                && !properties.getCluster().getNodes().isEmpty()) {
            RedisClusterConfiguration cluster =
                    new RedisClusterConfiguration(properties.getCluster().getNodes());
            if (properties.getCluster().getMaxRedirects() != null) {
                cluster.setMaxRedirects(properties.getCluster().getMaxRedirects());
            }
            if (properties.getUsername() != null) {
                cluster.setUsername(properties.getUsername());
            }
            if (properties.getPassword() != null) {
                cluster.setPassword(RedisPassword.of(properties.getPassword()));
            }
            return new LettuceConnectionFactory(cluster, clientConfiguration);
        }
        // Standalone
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(properties.getHost());
        standalone.setPort(properties.getPort());
        standalone.setDatabase(properties.getDatabase());
        if (properties.getUsername() != null) {
            standalone.setUsername(properties.getUsername());
        }
        if (properties.getPassword() != null) {
            standalone.setPassword(RedisPassword.of(properties.getPassword()));
        }
        return new LettuceConnectionFactory(standalone, clientConfiguration);
    }

    private LettuceClientConfiguration lettuceClientConfiguration(RedisProperties properties) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder;
        RedisProperties.Pool pool = properties.getLettuce().getPool();
        if (pool != null) {
            GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig =
                    new GenericObjectPoolConfig<>();
            poolConfig.setMaxTotal(pool.getMaxActive());
            poolConfig.setMaxIdle(pool.getMaxIdle());
            poolConfig.setMinIdle(pool.getMinIdle());
            if (pool.getMaxWait() != null) {
                poolConfig.setMaxWait(pool.getMaxWait());
            }
            builder = LettucePoolingClientConfiguration.builder()
                    .poolConfig(poolConfig);
        } else {
            builder = LettuceClientConfiguration.builder();
        }
        if (properties.getTimeout() != null) {
            builder.commandTimeout(properties.getTimeout());
        }
        if (properties.getClientName() != null) {
            builder.clientName(properties.getClientName());
        }
        if (properties.getSsl().isEnabled()) {
            builder.useSsl();
        }
        if (properties.getLettuce().getShutdownTimeout() != null) {
            builder.shutdownTimeout(properties.getLettuce().getShutdownTimeout());
        }
        if (properties.getConnectTimeout() != null) {
            SocketOptions socketOptions = SocketOptions.builder()
                    .connectTimeout(properties.getConnectTimeout())
                    .build();
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(socketOptions)
                    .build();
            builder.clientOptions(clientOptions);
        }
        return builder.build();
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
