package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.listener.CacheListener;

/**
 * @author R.Mehri
 * @since 6/17/202
 */
public class LocalCacheConfig {

    /**
     * Time to live and time to idle of item
     */
    private CacheExpiryPolicy expiryPolicy;
    /**
     * Cache listener: for raising events when an item removed or expired or ...
     */
    private CacheListener cacheListener;
    /**
     * maximum number of items in the cache. if maxSize reached lru algorithm used for removing an item
     */
    private int maxSize = Integer.MAX_VALUE;
    /**
     * clear other local caches using redis stream when this flag is true
     */
    private boolean needsClearCachePropagation = false;

    public CacheExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    public void setExpiryPolicy(CacheExpiryPolicy expiryPolicy) {
        this.expiryPolicy = expiryPolicy;
    }

    public CacheListener getCacheListener() {
        return cacheListener;
    }

    public void setCacheListener(CacheListener cacheListener) {
        this.cacheListener = cacheListener;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean getNeedsClearCachePropagation() {
        return needsClearCachePropagation;
    }

    public void setNeedsClearCachePropagation(boolean needsClearCachePropagation) {
        this.needsClearCachePropagation = needsClearCachePropagation;
    }
}
