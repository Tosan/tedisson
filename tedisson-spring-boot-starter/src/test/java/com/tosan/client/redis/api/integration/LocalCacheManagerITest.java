package com.tosan.client.redis.api.integration;

import com.tosan.client.redis.api.CacheExpiryPolicy;
import com.tosan.client.redis.api.LocalCacheManager;
import com.tosan.client.redis.api.listener.CacheListener;
import com.tosan.client.redis.cacheconfig.LocalCacheConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


class LocalCacheManagerITest extends BaseITest {

    public static final String CACHE_WITH_SIZE = "CACHE_WITH_SIZE";
    public static final String TEST_WITH_EXPIRY_TTL = "TEST_WITH_EXPIRY_TTL";
    public static final String TEST_WITH_EXPIRY_TTL_TTI = "TEST_WITH_EXPIRY_TTL_TTI";
    @Autowired
    private LocalCacheManager localCacheManager;

    @Test
    void testCreateCache() {
        String cacheName = "testCreateCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.createCache(CACHE_WITH_SIZE, getCacheConfig(null, null, 100));
        localCacheManager.createCache(TEST_WITH_EXPIRY_TTL, getCacheConfig(null, new CacheExpiryPolicy(10L), 100));
        localCacheManager.createCache(TEST_WITH_EXPIRY_TTL_TTI, getCacheConfig(null, new CacheExpiryPolicy(10L, 5L), 100));
        Assertions.assertTrue(localCacheManager.isCacheExist(cacheName));
        Assertions.assertTrue(localCacheManager.isCacheExist(CACHE_WITH_SIZE));
        Assertions.assertTrue(localCacheManager.isCacheExist(TEST_WITH_EXPIRY_TTL));
        Assertions.assertTrue(localCacheManager.isCacheExist(TEST_WITH_EXPIRY_TTL_TTI));
    }


    @Test
    void testClearCache() {
        String cacheName = "testClearCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.clearCache(cacheName);
        Assertions.assertTrue(localCacheManager.isCacheEmpty(cacheName));
    }

    @Test
    void testUpdateItemExpirationWIthTTL() throws InterruptedException {
        String cacheName = "testUpdateItemExpirationWIthTTL";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        localCacheManager.updateItemExpiration(cacheName, "key", 3L, null, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testUpdateItemExpirationWIthTTI() throws InterruptedException {
        String cacheName = "testUpdateItemExpirationWIthTTI";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        localCacheManager.updateItemExpiration(cacheName, "key", null, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testUpdateItemExpirationWIthTTL_AND_TTI() throws InterruptedException {
        String cacheName = "testUpdateItemExpirationWIthTTL_AND_TTI";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        localCacheManager.updateItemExpiration(cacheName, "key", 4L, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testRemoveCache() {
        String cacheName = "testRemoveCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.removeCache(cacheName);
        Assertions.assertFalse(localCacheManager.isCacheExist(cacheName));
    }

    @Test
    void testIsKeyInCache() {
        String cacheName = "testIsKeyInCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        Assertions.assertTrue(localCacheManager.isKeyInCache(cacheName, "key"));
    }

    @Test
    void testIncrementAndGetAtomicItem() {
        String cacheName = "testIncrementAndGetAtomicItem";
        localCacheManager.createCache(cacheName);
        Assertions.assertEquals(1, localCacheManager.incrementAndGetAtomicItem(cacheName, "key"));
    }

    @Test
    void testResetAtomicItem() {
        String cacheName = "testResetAtomicItem";
        localCacheManager.createCache(cacheName);
        localCacheManager.incrementAndGetAtomicItem(cacheName, "key");
        localCacheManager.resetAtomicItem(cacheName, "key");
        Assertions.assertEquals(0, localCacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testGetAtomicItem() {
        String cacheName = "testGetAtomicItem";
        localCacheManager.createCache(cacheName);
        Assertions.assertEquals(0, localCacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testSetAtomicItem() {
        String cacheName = "testSetAtomicItem";
        localCacheManager.createCache(cacheName);
        localCacheManager.setAtomicItem(cacheName, "key", 3L);
        Assertions.assertEquals(3, localCacheManager.getAtomicValue(cacheName, "key"));
    }

    @Test
    void testGetCacheKeySet() {
        String cacheName = "testGetCacheKeySet";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.addItemToCache(cacheName, "key1", "value1");
        Set<String> keySet = localCacheManager.getCacheKeySet(cacheName);
        Assertions.assertEquals(2, keySet.size());
        Assertions.assertTrue(keySet.contains("key"));
        Assertions.assertTrue(keySet.contains("key1"));
    }

    @Test
    void testGetAllFromCache() {
        String cacheName = "testGetAllFromCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.addItemToCache(cacheName, "key1", "value1");
        List<String> list = localCacheManager.getAllFromCache(cacheName);
        Assertions.assertEquals(2, list.size());
        Assertions.assertTrue(list.contains("value"));
        Assertions.assertTrue(list.contains("value1"));
    }

    @Test
    void testIsCacheExist() {
        String cacheName = "testIsCacheExist";
        localCacheManager.createCache(cacheName);
        Assertions.assertTrue(localCacheManager.isCacheExist(cacheName));
    }

    @Test
    void testRemoveItemFromCache() {
        String cacheName = "testRemoveItemFromCache";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.removeItemFromCache(cacheName, "key");
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testReplaceCacheItem() {
        String cacheName = "testReplaceCacheItem";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.replaceCacheItem(cacheName, "key", "value1");
        Assertions.assertEquals("value1", localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testGetCacheSize() {
        String cacheName = "testGetCacheSize";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value");
        localCacheManager.addItemToCache(cacheName, "key1", "value1");
        Assertions.assertEquals(2, localCacheManager.getCacheSize(cacheName));
    }

    @Test
    void testIsCacheEmpty() {
        String cacheName = "testIsCacheEmpty";
        localCacheManager.createCache(cacheName);
        Assertions.assertTrue(localCacheManager.isCacheEmpty(cacheName));
    }

    @Test
    void testTTLExpiration() throws InterruptedException {
        String cacheName = "testTTLExpiration";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(3000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTL() throws InterruptedException {
        String cacheName = "testTTL";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIExpiration() throws InterruptedException {
        String cacheName = "testTTIExpiration";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", null, 2L, TimeUnit.SECONDS);
        Thread.sleep(3000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTI() throws InterruptedException {
        String cacheName = "testTTI";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", null, 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLExpirationWithTTLAndTTI() throws InterruptedException {
        String cacheName = "testTTLExpirationWithTTLAndTTI";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 5L, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        localCacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        localCacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIExpirationWithTTLAndTTI() throws InterruptedException {
        String cacheName = "testTTIExpirationWithTTLAndTTI";
        localCacheManager.createCache(cacheName);
        localCacheManager.addItemToCache(cacheName, "key", "value", 5L, 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLExpirationWithExpiryPolicy() throws InterruptedException {
        String cacheName = "testTTLExpirationWithExpiryPolicy";
        localCacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(2L), 100));
        localCacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(3000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLWithExpiryPolicy() throws InterruptedException {
        String cacheName = "testTTLWithExpiryPolicy";
        localCacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(3L), 100));
        localCacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIExpirationWithExpiryPolicy() throws InterruptedException {
        String cacheName = "testTTIExpirationWithExpiryPolicy";
        localCacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(4L, 2L), 100));
        localCacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(3000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTIWithExpiryPolicy() throws InterruptedException {
        String cacheName = "testTTIWithExpiryPolicy";
        localCacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(4L, 3L), 100));
        localCacheManager.addItemToCache(cacheName, "key", "value");
        Thread.sleep(2000);
        Assertions.assertNotNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testTTLExpirationWithTTLAndTTIAndExpiryPolicy() throws InterruptedException {
        String cacheName = "testTTLExpirationWithTTLAndTTIAndExpiryPolicy";
        localCacheManager.createCache(cacheName, getCacheConfig(null, new CacheExpiryPolicy(5L, 3L), 100));
        Thread.sleep(2000);
        localCacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        localCacheManager.getItemFromCache(cacheName, "key");
        Thread.sleep(2000);
        Assertions.assertNull(localCacheManager.getItemFromCache(cacheName, "key"));
    }

    @Test
    void testExpireAtomicItemExpiration() throws InterruptedException {
        String cacheName = "testExpireAtomicItemExpiration";
        localCacheManager.createCache(cacheName);
        localCacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey");
        localCacheManager.expireAtomicItem(cacheName, "atomicKey", 2L, TimeUnit.SECONDS);
        Thread.sleep(3000);
        Assertions.assertEquals(1, localCacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey"));
    }

    @Test
    void testUpdateAtomicItemExpiration() throws InterruptedException {
        String cacheName = "testUpdateAtomicItemExpiration";
        localCacheManager.createCache(cacheName);
        localCacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey");
        localCacheManager.expireAtomicItem(cacheName, "atomicKey", 2L, TimeUnit.SECONDS);
        Thread.sleep(1000);
        localCacheManager.expireAtomicItem(cacheName, "atomicKey", 3L, TimeUnit.SECONDS);
        Thread.sleep(2000);
        Assertions.assertEquals(2, localCacheManager.incrementAndGetAtomicItem(cacheName, "atomicKey"));
    }

    @Test
    void testGetCacheExpirationConfig() {
        String cacheName = "testGetCacheExpirationConfig";
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        CacheExpiryPolicy expiryPolicy = new CacheExpiryPolicy(10L, 5L);
        cacheConfig.setExpiryPolicy(expiryPolicy);
        localCacheManager.createCache(cacheName, cacheConfig);
        Assertions.assertEquals(10L, localCacheManager.getCacheExpirationConfig(cacheName).getTimeToLiveSecond());
        Assertions.assertEquals(5L, localCacheManager.getCacheExpirationConfig(cacheName).getTimeToIdleSecond());
    }

    @Test
    void testReplaceCacheExpirationConfig() {
        String cacheName = "testReplaceCacheExpirationConfig";
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        CacheExpiryPolicy expiryPolicy = new CacheExpiryPolicy(10L, 5L);
        cacheConfig.setExpiryPolicy(expiryPolicy);
        localCacheManager.createCache(cacheName, cacheConfig);
        CacheExpiryPolicy newExpiryPolicy = new CacheExpiryPolicy(20L, 10L);
        localCacheManager.replaceCacheExpirationConfig(cacheName, newExpiryPolicy);
        localCacheManager.replaceCacheExpirationConfig(cacheName, newExpiryPolicy);
        Assertions.assertEquals(20L, localCacheManager.getCacheExpirationConfig(cacheName).getTimeToLiveSecond());
        Assertions.assertEquals(10L, localCacheManager.getCacheExpirationConfig(cacheName).getTimeToIdleSecond());
    }

    private LocalCacheConfig getCacheConfig(CacheListener cacheListener, CacheExpiryPolicy expiryPolicy, int maxSize) {
        LocalCacheConfig cacheConfig = new LocalCacheConfig();
        cacheConfig.setExpiryPolicy(expiryPolicy);
        cacheConfig.setCacheListener(cacheListener);
        cacheConfig.setMaxSize(maxSize);
        return cacheConfig;
    }
}