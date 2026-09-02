package com.tosan.client.redis.listener;

import com.tosan.client.redis.api.listener.LettuceListener;
import com.tosan.client.redis.enumuration.LettuceListenerEventType;

public class SampleLettuceListener implements LettuceListener {

    @Override
    public void onMessage(String cacheName, String key, Object value, LettuceListenerEventType eventType) {
        System.out.println(cacheName);
        System.out.println(key);
        System.out.println(value);
        System.out.println(eventType);
    }
}