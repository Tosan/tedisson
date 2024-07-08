package com.tosan.client.redis.exception;

/**
 * @author R.Mehri
 * @since 1/9/2023
 */
public class TedissonRuntimeException extends RuntimeException {

    public TedissonRuntimeException() {
    }

    public TedissonRuntimeException(String message) {
        super(message);
    }

    public TedissonRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TedissonRuntimeException(Throwable cause) {
        super(cause);
    }

    public TedissonRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
