package com.tosan.client.redis.enumuration;

/**
 * @author R.Mehri
 * @since 7/11/2023
 */
public enum RedisConnectionType {

    SINGLE_NODE,
    CLUSTER,
    MASTER_SLAVE,
    SENTINEL,
    REPLICATED
}
