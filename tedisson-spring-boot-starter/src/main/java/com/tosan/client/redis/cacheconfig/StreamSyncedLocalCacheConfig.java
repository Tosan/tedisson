package com.tosan.client.redis.cacheconfig;

import com.tosan.client.redis.enumuration.CentralCacheType;

/**
 * @author R.Mehri
 * @since 1/14/2023
 */
public class StreamSyncedLocalCacheConfig extends CentralCacheTypeConfig {

    public StreamSyncedLocalCacheConfig() {
        super(CentralCacheType.STREAM_SYNCED_LOCAL);
    }
}
