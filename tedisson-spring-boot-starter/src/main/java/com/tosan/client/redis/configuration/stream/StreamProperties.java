package com.tosan.client.redis.configuration.stream;

import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author R.Mehri
 * @since 6/20/2021
 */
@Data
public class StreamProperties {

    private boolean enabled = false;
    private Long maxMessageSize = 20L;
    private Integer trimRateSecond = 3600;

    @NestedConfigurationProperty
    private ThreadPoolProperties threadPool = new ThreadPoolProperties();
}
