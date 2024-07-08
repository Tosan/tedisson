package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.api.CacheExpiryPolicy;

/**
 * @author R.Mehri
 * @since 6/19/2023
 */
public class LocalRunTimeCacheConfig {

    private boolean needsClearCachePropagation = false;
    private CacheExpiryPolicy expiryPolicy;

    public boolean getNeedsClearCachePropagation() {
        return needsClearCachePropagation;
    }

    public void setNeedsClearCachePropagation(boolean needsClearCachePropagation) {
        this.needsClearCachePropagation = needsClearCachePropagation;
    }

    public CacheExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    public void setExpiryPolicy(CacheExpiryPolicy expiryPolicy) {
        this.expiryPolicy = expiryPolicy;
    }
}
