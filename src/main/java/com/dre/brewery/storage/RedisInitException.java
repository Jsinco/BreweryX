package com.dre.brewery.storage;

public class RedisInitException extends Exception {

    public RedisInitException(String message) {
        super(message);
    }

    public RedisInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
