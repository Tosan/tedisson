package com.tosan.client.redis.configuration.redisson;

import lombok.Data;

/**
 * @author R.Mehri
 * @since 1/7/2023
 */
//org.redisson.config.SingleServerConfig
@Data
public class SingleServerProperties {

    /**
     * Redis server address
     */
    private String address;

    /**
     * Minimum idle subscription connection amount
     */
    private Integer subscriptionConnectionMinimumIdleSize = 5;

    /**
     * Redis subscription connection maximum pool size
     */
    private Integer subscriptionConnectionPoolSize = 50;

    /**
     * Minimum idle Redis connection amount
     */
    private Integer connectionMinimumIdleSize = 24;

    /**
     * Redis connection maximum pool size
     */
    private Integer connectionPoolSize = 64;

    /**
     * Database index used for Redis connection
     */
    private Integer database = 0;

    /**
     * Interval in milliseconds to check DNS
     */
    private Long dnsMonitoringInterval = 5000L;
}
