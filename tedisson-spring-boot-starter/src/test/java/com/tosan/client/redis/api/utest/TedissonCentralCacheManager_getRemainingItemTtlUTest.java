package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.cacheconfig.ListenerSyncedLocalCacheConfig;
import com.tosan.client.redis.cacheconfig.StreamSyncedLocalCacheConfig;
import com.tosan.client.redis.util.CacheTtlUtil;
import com.tosan.client.redis.impl.redisson.TedissonCentralCacheManagerImpl;
import com.tosan.client.redis.api.LocalCacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TedissonCentralCacheManager_getRemainingItemTtlUTest {

    private static final String CACHE_NAME = "central-cache";
    private static final String KEY = "item-key";

    @Mock
    private RedissonClient redissonClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RMapCache mapCache;

    @Mock
    private LocalCacheManager localCacheManager;

    private CacheTtlUtil cacheTtlUtil;
    private TedissonCentralCacheManagerImpl centralCacheManager;

    @BeforeEach
    void setUp() {
        cacheTtlUtil = new CacheTtlUtil();
        centralCacheManager = new TedissonCentralCacheManagerImpl(redissonClient, cacheTtlUtil);
        centralCacheManager.setLocalCacheManager(localCacheManager);
    }

    @Test
    void getRemainingItemTtl_nonSyncedLocalUsesRedisTtl() {
        long redisRemainingMs = 5000L;
        when(redissonClient.getMapCache(CACHE_NAME)).thenReturn(mapCache);
        when(mapCache.remainTimeToLive(KEY)).thenReturn(redisRemainingMs);

        long result = centralCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);

        assertEquals(cacheTtlUtil.convertRedisRemainingTtl(redisRemainingMs, TimeUnit.SECONDS), result);
        verify(localCacheManager, never()).getRemainingItemTtl(anyString(), anyString(), any(TimeUnit.class));
    }

    @Test
    void getRemainingItemTtl_syncedLocalDelegatesToLocalCacheManager() {
        long localTtl = 42L;
        when(localCacheManager.isKeyInCache(CACHE_NAME, KEY)).thenReturn(true);
        when(localCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS)).thenReturn(localTtl);

        centralCacheManager.setCacheType(CACHE_NAME, new ListenerSyncedLocalCacheConfig(false, false, false));
        long listenerSyncedResult = centralCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);

        assertEquals(localTtl, listenerSyncedResult);

        centralCacheManager.setCacheType(CACHE_NAME, new StreamSyncedLocalCacheConfig());
        long streamSyncedResult = centralCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);

        assertEquals(localTtl, streamSyncedResult);
        verify(localCacheManager, times(2)).getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);
        verify(mapCache, never()).remainTimeToLive(anyString());
    }

    @Test
    void getRemainingItemTtl_redisSentinelsPassThroughUnchanged() {
        when(redissonClient.getMapCache(CACHE_NAME)).thenReturn(mapCache);
        when(mapCache.remainTimeToLive(KEY)).thenReturn(CacheTtlUtil.KEY_NOT_FOUND);

        long keyNotFoundResult = centralCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);

        assertEquals(CacheTtlUtil.KEY_NOT_FOUND, keyNotFoundResult);

        when(mapCache.remainTimeToLive(KEY)).thenReturn(CacheTtlUtil.NO_EXPIRE);

        long noExpireResult = centralCacheManager.getRemainingItemTtl(CACHE_NAME, KEY, TimeUnit.SECONDS);

        assertEquals(CacheTtlUtil.NO_EXPIRE, noExpireResult);
    }
}
