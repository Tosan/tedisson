package com.tosan.client.redis.api.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineElement;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author R.Mehri
 * @since 4/18/2023
 */
public interface CaffeineCacheListener extends RemovalListener<String, CaffeineElement>, CacheListener {

    void onRemoval(@Nullable String key, @Nullable CaffeineElement value, @NonNull RemovalCause removalCause);
}
