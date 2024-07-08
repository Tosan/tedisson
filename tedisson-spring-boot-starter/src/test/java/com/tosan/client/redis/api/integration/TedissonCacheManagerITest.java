package com.tosan.client.redis.api.integration;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

class TedissonCacheManagerITest extends BaseITest {

    @Autowired
    private TedissonCacheManager cacheManager;

    @Test
    void isKeyInCache() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        Assertions.assertTrue(cacheManager.isKeyInCache(cacheName, "key"));
    }

    @Test
    void testAddAllToCache() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.addItemToCache(cacheName, "key1", "value1");
        Map<String, Object> items = new HashMap<>();
        items.put("key", "value");
        items.put("key1", "value1");
        cacheManager.addAllToCache(cacheName, items);
        Assertions.assertEquals("value", cacheManager.getItemFromCache(cacheName, "key"));
        Assertions.assertEquals("value1", cacheManager.getItemFromCache(cacheName, "key1"));
    }

    @Test
    void testReplaceCacheItem() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.replaceCacheItem(cacheName, "key", "value1");
        Assertions.assertEquals("value1", cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testRemoveItemFromCache() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.removeItemFromCache(cacheName, "key");
        Assertions.assertNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testClearCache() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.clearCache(cacheName);
        Assertions.assertTrue(cacheManager.isCacheEmpty(cacheName));
    }

    @Test
    void testIsCacheEmpty() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        Assertions.assertTrue(cacheManager.isCacheEmpty(cacheName));
    }

    @Test
    void testGetCacheSize() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.addItemToCache(cacheName, "key1", "value1");
        Assertions.assertEquals(2, cacheManager.getCacheSize(cacheName));
    }

    @Test
    void testGetCacheKeySet() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value");
        cacheManager.addItemToCache(cacheName, "key1", "value1");
        Set<String> keySet = cacheManager.getCacheKeySet(cacheName);
        Assertions.assertEquals(2, keySet.size());
        Assertions.assertTrue(keySet.contains("key"));
        Assertions.assertTrue(keySet.contains("key1"));
    }

    @Test
    void testIncrementAndGetAtomicItem() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        Assertions.assertEquals(1, cacheManager.incrementAndGetAtomicItem(cacheName, "key"));
    }

    @Test
    void testResetAtomicItem() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.incrementAndGetAtomicItem(cacheName, "key");
        cacheManager.resetAtomicItem(cacheName, "key");
        Assertions.assertEquals(0, cacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testGetAtomicValue() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        Assertions.assertEquals(0, cacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testSetAtomicItem() {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.setAtomicItem(cacheName, "key", 3L);
        Assertions.assertEquals(3, cacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testUpdateItemExpirationWIthTTL() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        cacheManager.updateItemExpiration(cacheName, "key", 3L, null, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testUpdateItemExpirationWIthTTI() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        cacheManager.updateItemExpiration(cacheName, "key", null, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testUpdateItemExpirationWIthTTL_AND_TTI() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        cacheManager.updateItemExpiration(cacheName, "key", 4L, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLExpirationWithExpiryPolicy() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(2L), 100));
        cacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(3000);
        Assertions.assertNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLWithExpiryPolicy() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(3L), 100));
        cacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(2000);
        Assertions.assertNotNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIExpirationWithExpiryPolicy() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(4L, 2L), 100));
        cacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(3000);
        Assertions.assertNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIWithExpiryPolicy() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(4L, 3L), 100));
        cacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(2000);
        Assertions.assertNotNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLExpirationWithTTLAndTTIAndExpiryPolicy() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(5L, 3L), 100));
        Thread.sleep(2000);
        cacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        cacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        Assertions.assertNull(cacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testExpireAtomicItemExpiration() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey");
        cacheManager.expireAtomicItem(cacheName, "atomicKey", 2L, TimeUnit.SECONDS);
        Thread.sleep(3000);
        Assertions.assertEquals(1, cacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey"));
    }

    @Test
    void testUpdateAtomicItemExpiration() throws InterruptedException {
        String cacheName = getCacheName();
        cacheManager.createCache(cacheName);
        cacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey");
        cacheManager.expireAtomicItem(cacheName, "atomicKey", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        cacheManager.expireAtomicItem(cacheName, "atomicKey", 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertEquals(2, cacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey"));
    }

    @Test
    void testGetCacheExpirationConfig() {
        String cacheName = getCacheName();
        CacheConfig cacheConfig = new CacheConfig();
        CacheExpiryPolicy expiryPolicy = new CacheExpiryPolicy(10L, 5L);
        cacheConfig.setExpiryPolicy(expiryPolicy);
        cacheManager.createCache(cacheName, cacheConfig);
        Assertions.assertEquals(10L, cacheManager.getCacheExpirationConfig(cacheName).getTimeToLiveSecond());
        Assertions.assertEquals(5L, cacheManager.getCacheExpirationConfig(cacheName).getTimeToIdleSecond());
    }

    @Test
    void testReplaceCacheExpirationConfig() {
        String cacheName = getCacheName();
        CacheConfig cacheConfig = new CacheConfig();
        CacheExpiryPolicy expiryPolicy = new CacheExpiryPolicy(10L, 5L);
        cacheConfig.setExpiryPolicy(expiryPolicy);
        cacheManager.createCache(cacheName, cacheConfig);
        CacheExpiryPolicy newExpiryPolicy = new CacheExpiryPolicy(20L, 10L);
        cacheManager.replaceCacheExpirationConfig(cacheName, newExpiryPolicy);
        cacheManager.replaceCacheExpirationConfig(cacheName, newExpiryPolicy);
        Assertions.assertEquals(20L, cacheManager.getCacheExpirationConfig(cacheName).getTimeToLiveSecond());
        Assertions.assertEquals(10L, cacheManager.getCacheExpirationConfig(cacheName).getTimeToIdleSecond());
    }


    private CacheConfig getCacheConfig(CacheListener cacheListener, CacheExpiryPolicy expiryPolicy, int maxSize) {
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setExpiryPolicy(expiryPolicy);
        cacheConfig.setListeners(Collections.singletonList(cacheListener));
        cacheConfig.setMaxSize(maxSize);
        return cacheConfig;
    }

    private String getCacheName() {
        return UUID.randomUUID().toString();
    }
}