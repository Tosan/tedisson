package com.tosan.client.redis.impl.redisson;

import java.io.Serializable;

/**
 * @author R.Mehri
 * @since 7/6/2022
 */
public class CacheElement implements Serializable {
    private static final long serialVersionUID = 4329642548497478371L;
    private final String instanceID;
    private Object data;

    public CacheElement(Object data, String instanceID) {
        this.data = data;
        this.instanceID = instanceID;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getInstanceID() {
        return instanceID;
    }
}
