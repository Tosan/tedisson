package com.tosan.client.redis.exception;

/**
 * @author R.Mehri
 * @since 1/9/2023
 */
public class TedissonException extends Exception {

    public TedissonException() {
    }

    public TedissonException(String message) {
        super(message);
    }

    public TedissonException(String message, Throwable cause) {
        super(message, cause);
    }

    public TedissonException(Throwable cause) {
        super(cause);
    }

    public TedissonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
