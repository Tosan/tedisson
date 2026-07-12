package com.tosan.client.redis.configuration.redisson;

import com.tosan.client.redis.configuration.LocalProperties;
import com.tosan.client.redis.enumuration.RedisClientType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author R.Mehri
 * @since 12/26/2022
 */
@ConfigurationProperties(prefix = "tedisson")
@Data
public class TedissonProperties {

    /**
     * Redis client type: REDISSON or LETTUCE
     * Default is REDISSON.
     * When LETTUCE, the connection is provided by Spring Boot's auto-configuration
     * via standard spring.data.redis.* properties.
     */
    private RedisClientType redisClientType = RedisClientType.REDISSON;

    /**
     * Local Cache Properties
     */
    @NestedConfigurationProperty
    private LocalProperties local;

    /**
     * Redis properties (for Redisson client)
     */
    @NestedConfigurationProperty
    private RedisProperties redis;
}
