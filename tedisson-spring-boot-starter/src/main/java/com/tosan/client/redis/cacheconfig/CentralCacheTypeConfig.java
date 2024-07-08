package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.enumuration.CentralCacheType;

/**
 * @author R.Mehri
 * @since 12/4/2022
 */
public class CentralCacheTypeConfig {
    protected CentralCacheType centralCacheType;


    public CentralCacheTypeConfig(CentralCacheType centralCacheType) {
        this.centralCacheType = centralCacheType;
    }

    public CentralCacheType getCentralCacheType() {
        return centralCacheType;
    }

    public void setCentralCacheType(CentralCacheType centralCacheType) {
        this.centralCacheType = centralCacheType;
    }
}
