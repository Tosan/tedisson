package com.tosan.client.redis.impl.localCacheManager.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.concurrent.TimeUnit;

/**
 * @author R.Mehri
 * @since 4/18/2023
 */
public class CaffeineExpiry implements Expiry<String, CaffeineElement> {


    @Override
    public long expireAfterCreate(String key, CaffeineElement element, long currentTime) {
        if (element == null) {
            return currentTime;
        }
        if (element.getTimeToIdleSecond() != null) {
            return getTimeInNanoSeconds(element.getTimeToIdleSecond());
        } else if (element.getTimeToLiveSecond() != null) {
            return getTimeInNanoSeconds(element.getTimeToLiveSecond());
        }
        return currentTime;
    }

    @Override
    public long expireAfterUpdate(String key, CaffeineElement element, long currentTime, @NonNegative long currentDuration) {
        if (element == null || element.getTimeToIdleSecond() == null) {
            return currentDuration;
        }
        if (element.getExpirationTimeNano() != null && ((currentTime + getTimeInNanoSeconds(element.getTimeToIdleSecond())) > element.getExpirationTimeNano())) {
            return element.getExpirationTimeNano() - currentTime;
        }
        return TimeUnit.SECONDS.toNanos(element.getTimeToIdleSecond());
    }

    @Override
    public long expireAfterRead(String key, CaffeineElement element, long currentTime, @NonNegative long currentDuration) {
        if (element == null || element.getTimeToIdleSecond() == null) {
            return currentDuration;
        }
        if (element.getExpirationTimeNano() != null && ((currentTime + getTimeInNanoSeconds(element.getTimeToIdleSecond())) >
                element.getExpirationTimeNano())) {
            return element.getExpirationTimeNano() - currentTime;
        }
        return TimeUnit.SECONDS.toNanos(element.getTimeToIdleSecond());
    }

    private long getTimeInNanoSeconds(long second) {
        return TimeUnit.SECONDS.toNanos(second);
    }
}
