package com.tosan.client.redis.impl.lettuce;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author R.Mehri
 * @since 7/6/2022
 */
public class LettuceCacheElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 4329642548497478371L;
    private String instanceID;
    private Object data;
    private Long expirationTime;
    private Long timeToIdle;

    public LettuceCacheElement() {
    }

    public LettuceCacheElement(Object data, String instanceID) {
        this.data = data;
        this.instanceID = instanceID;
    }

    public LettuceCacheElement(Object data, String instanceID, Long expireDate) {
        this.data = data;
        this.instanceID = instanceID;
        this.expirationTime = expireDate;
    }

    public LettuceCacheElement(Object data, String instanceID, Long expireDate, Long timeToIdle) {
        this.data = data;
        this.instanceID = instanceID;
        this.expirationTime = expireDate;
        this.timeToIdle = timeToIdle;
    }

    public Object getData() {
        return data;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public Long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Long getTimeToIdle() {
        return timeToIdle;
    }

    public void setTimeToIdle(Long timeToIdle) {
        this.timeToIdle = timeToIdle;
    }

    public void setData(Object data) {
        this.data = data;
    }
}