package com.tosan.client.redis.impl.localCacheManager.ehcache;

import java.io.Serializable;
import java.util.Date;

/**
 * @author R.Mehri
 * @since 2/9/2022
 */
public class EhCacheElement implements Serializable, Cloneable {
    private static final long serialVersionUID = 6904710213069041016L;
    private Long timeToLiveSecond;
    private Long timeToIdleSecond;
    private Date expirationTime;
    private Date maxAllowedAccessTime;
    private Object value;

    public EhCacheElement(Long timeToLiveSecond, Long timeToIdleSecond, Date expirationTime, Date maxAllowedAccessTime, Object value) {
        this.timeToLiveSecond = timeToLiveSecond;
        this.timeToIdleSecond = timeToIdleSecond;
        this.expirationTime = expirationTime;
        this.maxAllowedAccessTime = maxAllowedAccessTime;
        this.value = value;
    }

    public EhCacheElement() {
    }

    public EhCacheElement(Object value) {
        this.value = value;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Date getMaxAllowedAccessTime() {
        return maxAllowedAccessTime;
    }

    public void setMaxAllowedAccessTime(Date maxAllowedAccessTime) {
        this.maxAllowedAccessTime = maxAllowedAccessTime;
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
}
