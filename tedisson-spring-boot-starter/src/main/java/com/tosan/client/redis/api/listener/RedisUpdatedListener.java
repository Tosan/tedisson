package com.tosan.client.redis.api.listener;

import com.tosan.client.redis.impl.redisson.CacheElement;
import org.redisson.api.map.event.EntryUpdatedListener;

/**
 * @author R.Mehri
 * @since 5/29/2023
 */
public interface RedisUpdatedListener extends EntryUpdatedListener<String, CacheElement>, CacheListener {
}
