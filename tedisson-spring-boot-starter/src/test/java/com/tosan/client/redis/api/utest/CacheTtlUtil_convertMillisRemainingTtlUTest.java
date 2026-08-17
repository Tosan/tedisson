package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.util.CacheTtlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheTtlUtil_convertMillisRemainingTtlUTest {

    private CacheTtlUtil cacheTtlUtil;

    @BeforeEach
    void setUp() {
        cacheTtlUtil = new CacheTtlUtil();
    }

    @Test
    void convertMillisRemainingTtl_positiveMillisecondsIdentity() {
        long remainingMs = 1500L;
        long result = cacheTtlUtil.convertMillisRemainingTtl(remainingMs, TimeUnit.MILLISECONDS);
        assertEquals(remainingMs, result);
    }

    @Test
    void convertMillisRemainingTtl_zeroOrNegativeReturnsZero() {
        assertEquals(0L, cacheTtlUtil.convertMillisRemainingTtl(0L, TimeUnit.MILLISECONDS));
        assertEquals(0L, cacheTtlUtil.convertMillisRemainingTtl(-100L, TimeUnit.MILLISECONDS));
    }
}
