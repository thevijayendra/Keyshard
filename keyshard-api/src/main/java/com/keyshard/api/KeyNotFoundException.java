package com.keyshard.api;

import lombok.Getter;

@Getter
public class KeyNotFoundException extends RuntimeException {
    private final String key;

    public KeyNotFoundException(String key) {
        super("Key not found in KeyShard: " + key);
        this.key = key;
    }
}