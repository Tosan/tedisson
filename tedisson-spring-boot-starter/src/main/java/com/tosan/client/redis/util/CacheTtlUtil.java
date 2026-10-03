package com.tosan.client.redis.util;

import java.util.concurrent.TimeUnit;

public class CacheTtlUtil {

    public static final long KEY_NOT_FOUND = -2L;
    public static final long NO_EXPIRE = -1L;

    public long convertRedisRemainingTtl(long remainingMs, TimeUnit timeUnit) {
        if (remainingMs == KEY_NOT_FOUND || remainingMs == NO_EXPIRE) {
            return remainingMs;
        }
        if (remainingMs <= 0) {
            return 0L;
        }
        return timeUnit.convert(remainingMs, TimeUnit.MILLISECONDS);
    }

    public long convertNanosRemainingTtl(long remainingNanos, TimeUnit timeUnit) {
        if (remainingNanos <= 0) {
            return 0L;
        }
        return timeUnit.convert(remainingNanos, TimeUnit.NANOSECONDS);
    }

    public long convertMillisRemainingTtl(long remainingMs, TimeUnit timeUnit) {
        if (remainingMs <= 0) {
            return 0L;
        }
        return timeUnit.convert(remainingMs, TimeUnit.MILLISECONDS);
    }
}