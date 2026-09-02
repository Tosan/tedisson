package com.tosan.client.redis.configuration.serializer;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.serializers.DefaultSerializers;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author R.Mehri
 * @since 7/19/2026
 */
public class Kryo5RedisSerializer<T> implements RedisSerializer<T> {

    private final ThreadLocal<Kryo> kryoThreadLocal =
            ThreadLocal.withInitial(this::createKryo);

    private Kryo createKryo() {
        Kryo kryo = new Kryo();
        // Performance options
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        kryo.register(AtomicInteger.class, new DefaultSerializers.AtomicIntegerSerializer());
        kryo.setCopyReferences(true);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        return kryo;
    }


    @Override
    public byte[] serialize(T value) {
        if (value == null) {
            return new byte[0];
        }
        Kryo kryo = kryoThreadLocal.get();
        Output output = new Output(512, -1);
        try {
            kryo.writeClassAndObject(output, value);
            return output.toBytes();
        } finally {
            output.close();
            // Important because Kryo keeps internal state
            kryo.reset();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Kryo kryo = kryoThreadLocal.get();
        Input input = new Input(new ByteArrayInputStream(bytes));
        try {
            return (T) kryo.readClassAndObject(input);
        } finally {
            input.close();
            kryo.reset();
        }
    }
    
    public void cleanup() {
        kryoThreadLocal.remove();
    }
}