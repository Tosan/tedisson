package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.util.CacheTtlUtil;
import com.tosan.client.redis.impl.localCacheManager.ehcache.EhCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EhCacheManager_getRemainingItemTtlUTest {

    private EhCacheManager ehCacheManager;

    @BeforeEach
    void setUp() {
        ehCacheManager = new EhCacheManager(new CacheTtlUtil());
    }

    @Test
    void getRemainingItemTtl_futureExpirationTimeOnly() {
        String cacheName = "ttl-" + UUID.randomUUID();
        String key = "key";
        ehCacheManager.createCache(cacheName);
        ehCacheManager.addItemToCache(cacheName, key, "value", 60L, TimeUnit.SECONDS);

        long result = ehCacheManager.getRemainingItemTtl(cacheName, key, TimeUnit.SECONDS);

        assertTrue(result > 0L);
    }

    @Test
    void getRemainingItemTtl_expiredElementReturnsZero() throws InterruptedException {
        String cacheName = "ttl-" + UUID.randomUUID();
        String key = "key";
        ehCacheManager.createCache(cacheName);
        ehCacheManager.addItemToCache(cacheName, key, "value", 1L, TimeUnit.MILLISECONDS);
        Thread.sleep(50L);

        long result = ehCacheManager.getRemainingItemTtl(cacheName, key, TimeUnit.SECONDS);

        assertEquals(0L, result);
    }

    @Test
    void getRemainingItemTtl_nullKeyReturnsKeyNotFound() {
        long result = ehCacheManager.getRemainingItemTtl("any-cache", null, TimeUnit.SECONDS);

        assertEquals(CacheTtlUtil.KEY_NOT_FOUND, result);
    }

    @Test
    void getRemainingItemTtl_unknownCacheReturnsKeyNotFound() {
        long result = ehCacheManager.getRemainingItemTtl("non-existent-cache-" + UUID.randomUUID(), "key", TimeUnit.SECONDS);

        assertEquals(CacheTtlUtil.KEY_NOT_FOUND, result);
    }

    @Test
    void getRemainingItemTtl_missingKeyReturnsKeyNotFound() {
        String cacheName = "ttl-" + UUID.randomUUID();
        ehCacheManager.createCache(cacheName);

        long result = ehCacheManager.getRemainingItemTtl(cacheName, "missing-key", TimeUnit.SECONDS);

        assertEquals(CacheTtlUtil.KEY_NOT_FOUND, result);
    }
}
