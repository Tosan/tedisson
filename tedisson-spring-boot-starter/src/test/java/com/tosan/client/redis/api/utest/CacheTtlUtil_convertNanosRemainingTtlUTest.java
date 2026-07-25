package com.tosan.client.redis.api.utest;

import com.tosan.client.redis.util.CacheTtlUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheTtlUtil_convertNanosRemainingTtlUTest {

    private CacheTtlUtil cacheTtlUtil;

    @BeforeEach
    void setUp() {
        cacheTtlUtil = new CacheTtlUtil();
    }


    @Test
    void convertNanosRemainingTtl_positiveNanosecondsConverted() {
        long remainingNanos = TimeUnit.MILLISECONDS.toNanos(2L);

        assertEquals(
                TimeUnit.MICROSECONDS.convert(remainingNanos, TimeUnit.NANOSECONDS),
                cacheTtlUtil.convertNanosRemainingTtl(remainingNanos, TimeUnit.MICROSECONDS));
        assertEquals(
                TimeUnit.MILLISECONDS.convert(remainingNanos, TimeUnit.NANOSECONDS),
                cacheTtlUtil.convertNanosRemainingTtl(remainingNanos, TimeUnit.MILLISECONDS));
    }

    @Test
    void convertNanosRemainingTtl_zeroOrNegativeReturnsZero() {
        assertEquals(0L, cacheTtlUtil.convertNanosRemainingTtl(0L, TimeUnit.MILLISECONDS));
        assertEquals(0L, cacheTtlUtil.convertNanosRemainingTtl(-1L, TimeUnit.MILLISECONDS));
    }
}
