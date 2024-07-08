package com.tosan.client.redis.configuration.redisson;

import com.tosan.client.redis.configuration.LocalProperties;
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
     * Local Cache Properties
     */
    @NestedConfigurationProperty
    private LocalProperties local;

    /**
     * Redis properties
     */
    @NestedConfigurationProperty
    private RedisProperties redis;
}
