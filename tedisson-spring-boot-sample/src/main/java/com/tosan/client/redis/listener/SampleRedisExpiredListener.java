package com.tosan.client.redis.listener;

import com.tosan.client.redis.api.listener.RedisExpiredListener;
import com.tosan.client.redis.impl.redisson.CacheElement;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.map.event.EntryEvent;

/**
 * @author R.Mehri
 * @since 2/13/2023
 */
@Slf4j
public class SampleRedisExpiredListener implements RedisExpiredListener {

    @Override
    public void onExpired(EntryEvent<String, CacheElement> event) {
        log.info("Event {} raised with key:{}", event.getType(), event.getKey());
    }
}
