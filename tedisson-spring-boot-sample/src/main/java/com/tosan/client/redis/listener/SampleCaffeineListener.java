package com.tosan.client.redis.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.tosan.client.redis.api.listener.CaffeineCacheListener;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineElement;
import lombok.extern.slf4j.Slf4j;

/**
 * @author R.Mehri
 * @since 5/27/2023
 */
@Slf4j
public class SampleCaffeineListener implements CaffeineCacheListener {

    @Override
    public void onRemoval(String key, CaffeineElement value, RemovalCause removalCause) {
        log.info("Event {} raised with key: {}", removalCause, key);
    }
}
