package com.tosan.client.redis.api.listener;

import com.tosan.client.redis.impl.redisson.CacheElement;
import org.redisson.api.map.event.EntryExpiredListener;

/**
 * @author R.Mehri
 * @since 5/29/2023
 */
public interface RedisExpiredListener extends EntryExpiredListener<String, CacheElement>, CacheListener {
}
