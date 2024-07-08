package com.tosan.client.redis.stream.type;

/**
 * @author R.Mehri
 * @since 4/17/2022
 */
public enum StreamMessageListenerConsumerErrorType {
    NO_GROUP,
    NO_REPLICA,
    UNKNOWN,
    OTHER;
}
