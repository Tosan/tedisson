package com.tosan.client.redis.api;

import com.tosan.client.redis.exception.TedissonRuntimeException;

import java.io.Serializable;

/**
 * @author R.Mehri
 * @since 04/01/2021
 */
public class CacheExpiryPolicy implements Serializable {
    private static final long serialVersionUID = -1168883634273397186L;
    private Long timeToLiveSecond;
    private Long timeToIdleSecond;

    /**
     * @param timeToLiveSecond elapsed time for item expiration in second
     * @param timeToIdleSecond idle time for item expiration in second
     */
    public CacheExpiryPolicy(Long timeToLiveSecond, Long timeToIdleSecond) {
        validateCacheExpiryPolicy(timeToLiveSecond, timeToIdleSecond);
        this.timeToLiveSecond = timeToLiveSecond;
        this.timeToIdleSecond = timeToIdleSecond;
    }

    /**
     * @param timeToLiveSecond elapsed time for item expiration in second
     */
    public CacheExpiryPolicy(Long timeToLiveSecond) {
        this.timeToLiveSecond = timeToLiveSecond;
    }

    public Long getTimeToLiveSecond() {
        return timeToLiveSecond;
    }

    public void setTimeToLiveSecond(Long timeToLiveSecond) {
        this.timeToLiveSecond = timeToLiveSecond;
    }

    public Long getTimeToIdleSecond() {
        return timeToIdleSecond;
    }

    public void setTimeToIdleSecond(Long timeToIdleSecond) {
        this.timeToIdleSecond = timeToIdleSecond;
    }

    private void validateCacheExpiryPolicy(Long timeToLiveSecond, Long timeToIdleSecond) {
        if (timeToLiveSecond != null && timeToIdleSecond != null && timeToIdleSecond > timeToLiveSecond) {
            throw new TedissonRuntimeException("TimeToLive must be greater than TimeToIdle");
        }
    }
}
