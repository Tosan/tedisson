package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.enumuration.CentralCacheType;

/**
 * @author R.Mehri
 * @since 11/30/2022
 */
public class SharedCacheConfig extends CentralCacheTypeConfig {

    public SharedCacheConfig() {
        super(CentralCacheType.SHARED);
    }
}
