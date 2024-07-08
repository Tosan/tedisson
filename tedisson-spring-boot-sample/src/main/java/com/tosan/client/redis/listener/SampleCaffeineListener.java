package com.tosan.client.redis.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.tosan.client.redis.api.listener.CaffeineCacheListener;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineElement;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author R.Mehri
 * @since 5/27/2023
 */
@Slf4j
public class SampleCaffeineListener implements CaffeineCacheListener {

    @Override
    public void onRemoval(@Nullable String key, @Nullable CaffeineElement value, @NonNull RemovalCause removalCause) {
        log.info("Event {} raised with key:{}", removalCause, key);
    }
}
