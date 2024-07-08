package com.tosan.client.redis.impl.localCacheManager.caffeine;

import java.io.Serializable;

/**
 * @author R.Mehri
 * @since 2/9/2022
 */
public class CaffeineElement implements Serializable, Cloneable {
    private static final long serialVersionUID = 6904710213069041016L;
    private Long timeToLiveSecond;
    private Long timeToIdleSecond;
    private Long expirationTimeNano;
    private Object value;

    public CaffeineElement(Long timeToLiveSecond, Long timeToIdleSecond, Long expirationTimeNano, Object value) {
        this.timeToLiveSecond = timeToLiveSecond;
        this.timeToIdleSecond = timeToIdleSecond;
        this.expirationTimeNano = expirationTimeNano;
        this.value = value;
    }

    public CaffeineElement(Object value) {
        this.value = value;
    }

    public CaffeineElement() {
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getExpirationTimeNano() {
        return expirationTimeNano;
    }

    public void setExpirationTimeNano(Long expirationTimeNano) {
        this.expirationTimeNano = expirationTimeNano;
    }
}
