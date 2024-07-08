package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.listener.CacheListener;

import java.util.List;

/**
 * @author R.Mehri
 * @since 6/17/2023
 */
public class CacheConfig {

    private CacheExpiryPolicy expiryPolicy;
    private List<CacheListener> listeners;
    private int maxSize;
    private CentralCacheTypeConfig centralCacheTypeConfig;

    public CacheExpiryPolicy getExpiryPolicy() {
        return expiryPolicy;
    }

    public void setExpiryPolicy(CacheExpiryPolicy expiryPolicy) {
        this.expiryPolicy = expiryPolicy;
    }

    public List<CacheListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<CacheListener> listeners) {
        this.listeners = listeners;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public CentralCacheTypeConfig getCentralCacheType() {
        return centralCacheTypeConfig;
    }

    public void setCentralCacheType(CentralCacheTypeConfig centralCacheTypeConfig) {
        this.centralCacheTypeConfig = centralCacheTypeConfig;
    }
}
