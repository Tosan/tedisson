package com.tosan.client.redis.api.listener;

import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineElement;

/**
 * @author R.Mehri
 * @since 4/18/2023
 */
public interface CaffeineCacheListener extends RemovalListener<String, CaffeineElement>, CacheListener {

    void onRemoval(String key, CaffeineElement value, RemovalCause removalCause);
}
