package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.util.CacheTtlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheTtlUtil_convertRedisRemainingTtlUTest {

    private CacheTtlUtil cacheTtlUtil;

    @BeforeEach
    void setUp() {
        cacheTtlUtil = new CacheTtlUtil();
    }

    @Test
    void convertRedisRemainingTtl_positiveMillisecondsToSeconds() {
        long remainingMs = 5000L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        long result = cacheTtlUtil.convertRedisRemainingTtl(remainingMs, timeUnit);
        assertEquals(timeUnit.convert(remainingMs, TimeUnit.MILLISECONDS), result);
    }

    @Test
    void convertRedisRemainingTtl_keyNotFoundSentinel() {
        long result = cacheTtlUtil.convertRedisRemainingTtl(CacheTtlUtil.KEY_NOT_FOUND, TimeUnit.SECONDS);
        assertEquals(CacheTtlUtil.KEY_NOT_FOUND, result);
    }

    @Test
    void convertRedisRemainingTtl_noExpireSentinel() {
        long result = cacheTtlUtil.convertRedisRemainingTtl(CacheTtlUtil.NO_EXPIRE, TimeUnit.SECONDS);
        assertEquals(CacheTtlUtil.NO_EXPIRE, result);
    }
}
