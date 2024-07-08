package com.tosan.client.redis.stream;

import java.util.UUID;

/**
 * @author R.Mehri
 * @since 6/15/2021
 */
public class MessageParameter {
    public static final String TEDISSON_STREAM_NAME = "tedisson_stream_name";
    public static final String HOST_INSTANCE_ID = UUID.randomUUID().toString();
    public static final String TEDISSON_GROUP_NAME = "tedisson-" + UUID.randomUUID();
}
