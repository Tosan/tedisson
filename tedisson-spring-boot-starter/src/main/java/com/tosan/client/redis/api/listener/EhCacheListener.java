package com.tosan.client.redis.api.listener;

import com.tosan.client.redis.impl.localCacheManager.ehcache.EhCacheElement;
import org.ehcache.event.CacheEventListener;

/**
 * @author R.Mehri
 * @since 5/29/2023
 */
public interface EhCacheListener extends CacheEventListener<String, EhCacheElement>, CacheListener {
}
