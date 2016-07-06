package com.minehut.scaffold.config;

public class ConfigException extends RuntimeException {
    public ConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfigException(String msg) {
        super(msg);
    }
}
