package com.minehut.scaffold.config.inject;

import com.minehut.scaffold.config.ConfigException;

public class ConfigInjectionException extends ConfigException {
    public ConfigInjectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfigInjectionException(String msg) {
        super(msg);
    }
}
