package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.util.CacheTtlUtil;
import com.tosan.client.redis.impl.localCacheManager.caffeine.CaffeineCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaffeineCacheManager_getRemainingItemTtlUTest {

    private CaffeineCacheManager caffeineCacheManager;

    @BeforeEach
    void setUp() {
        caffeineCacheManager = new CaffeineCacheManager(new CacheTtlUtil());
    }

    @Test
    void getRemainingItemTtl_futureExpirationTimeNanoOnly() {
        String cacheName = "ttl-" + UUID.randomUUID();
        String key = "key";
        caffeineCacheManager.createCache(cacheName);
        caffeineCacheManager.addItemToCache(cacheName, key, "value", 60L, TimeUnit.SECONDS);
        Long result = caffeineCacheManager.getRemainingItemTtl(cacheName, key, TimeUnit.SECONDS);
        assertTrue(result > 0L);
    }

    @Test
    void getRemainingItemTtl_timeToIdleOnlyReturnsNull() {
        String cacheName = "ttl-" + UUID.randomUUID();
        String key = "key";
        caffeineCacheManager.createCache(cacheName);
        caffeineCacheManager.addItemToCache(cacheName, key, "value", null, 30L, TimeUnit.SECONDS);
        Long result = caffeineCacheManager.getRemainingItemTtl(cacheName, key, TimeUnit.SECONDS);
        assertNull(result);
    }

    @Test
    void getRemainingItemTtl_guardPathsReturnKeyNotFound() {
        String cacheName = "ttl-" + UUID.randomUUID();
        caffeineCacheManager.createCache(cacheName);
        assertEquals(CacheTtlUtil.KEY_NOT_FOUND,
                caffeineCacheManager.getRemainingItemTtl(cacheName, null, TimeUnit.SECONDS));
        assertEquals(CacheTtlUtil.KEY_NOT_FOUND,
                caffeineCacheManager.getRemainingItemTtl("unknown-" + UUID.randomUUID(), "key", TimeUnit.SECONDS));
        assertEquals(CacheTtlUtil.KEY_NOT_FOUND,
                caffeineCacheManager.getRemainingItemTtl(cacheName, "missing-key", TimeUnit.SECONDS));
    }

}
