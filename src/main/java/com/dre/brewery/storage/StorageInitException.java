package com.dre.brewery.storage;

public class StorageInitException extends Exception {
    public StorageInitException(String message) {
        super(message);
    }

    public StorageInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
