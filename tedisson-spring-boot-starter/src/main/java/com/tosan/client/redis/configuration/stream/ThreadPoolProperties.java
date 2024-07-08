package com.tosan.client.redis.configuration.stream;

import lombok.Data;

/**
 * @author R.Mehri
 * @since 6/15/2021
 */
@Data
public class ThreadPoolProperties {

    private Integer corePoolSize = 100;
    private Integer maxPoolSize = 200;
    private Integer queueCapacity = 1024;
}
