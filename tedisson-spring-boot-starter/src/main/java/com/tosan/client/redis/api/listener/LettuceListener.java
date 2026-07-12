package com.tosan.client.redis.api.listener;

import org.springframework.data.redis.connection.MessageListener;

public interface LettuceListener extends MessageListener, CacheListener {

}