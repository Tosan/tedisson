package com.tosan.client.redis.listener;

import com.tosan.client.redis.api.listener.EhCacheListener;
import com.tosan.client.redis.impl.localCacheManager.ehcache.EhCacheElement;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;

/**
 * @author R.Mehri
 * @since 2/13/2023
 */
@Slf4j
public class SampleEhCacheListener implements EhCacheListener {

    @Override
    public void onEvent(CacheEvent<? extends String, ? extends EhCacheElement> cacheEvent) {

        switch (cacheEvent.getType()) {
            //raised when an item removed. time to live or time to idle is over
            case REMOVED:
                //case EXPIRED: Not support
            case UPDATED:
            case CREATED:
            case EVICTED:
                log.info("Event {} raised with key:{}", cacheEvent.getType().toString(), cacheEvent.getKey());
        }
    }
}