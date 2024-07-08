package com.tosan.client.redis.api;

import lombok.Data;

/**
 * @author R.Mehri
 * @since 8/15/2023
 */
@Data
public class SpringCacheConfig {

    /**
     * Cache name
     */
    private String cacheName;
    /**
     * Item time to live. Item expired when time to live is overed
     */
    private long timeToLive;
    /**
     * Cache max number of items
     */
    private long maxSize;
}
