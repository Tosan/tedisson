package com.tosan.client.redis.listener;

import com.tosan.client.redis.api.listener.LettuceListener;
import org.springframework.data.redis.connection.Message;

public class SampleLettuceListener implements LettuceListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println(new String(message.getBody()));
        System.out.println(new String(message.getChannel()));
        System.out.println(new String(pattern));
    }
}