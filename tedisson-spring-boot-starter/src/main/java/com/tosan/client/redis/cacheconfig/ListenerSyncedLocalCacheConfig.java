package com.tosan.client.redis.cacheconfig;


import com.tosan.client.redis.enumuration.CentralCacheType;

/**
 * @author R.Mehri
 * @since 12/4/2022
 */
public class ListenerSyncedLocalCacheConfig extends CentralCacheTypeConfig {
    private final boolean needRemovedListener;
    private final boolean needUpdatedListener;
    private final boolean needCreatedListener;

    public ListenerSyncedLocalCacheConfig(boolean needRemovedListener, boolean needUpdatedListener,
                                          boolean needCreatedListener) {
        super(CentralCacheType.LISTENER_SYNCED_LOCAL);
        this.needRemovedListener = needRemovedListener;
        this.needUpdatedListener = needUpdatedListener;
        this.needCreatedListener = needCreatedListener;
    }

    public boolean isNeedRemovedListener() {
        return needRemovedListener;
    }

    public boolean isNeedUpdatedListener() {
        return needUpdatedListener;
    }

    public boolean isNeedCreatedListener() {
        return needCreatedListener;
    }
}
