package com.tosan.client.redis.listener;

import com.tosan.client.redis.api.listener.RedisUpdatedListener;
import com.tosan.client.redis.impl.redisson.CacheElement;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.map.event.EntryEvent;

/**
 * @author R.Mehri
 * @since 2/13/2023
 */
@Slf4j
public class SampleRedisUpdatedListener implements RedisUpdatedListener {

    @Override
    public void onUpdated(EntryEvent<String, CacheElement> event) {
        log.info("Event {} raised with key:{}", event.getType(), event.getKey());
    }
}
