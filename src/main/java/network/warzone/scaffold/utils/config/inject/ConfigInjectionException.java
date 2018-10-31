package network.warzone.scaffold.utils.config.inject;

import network.warzone.scaffold.utils.config.ConfigException;

public class ConfigInjectionException extends ConfigException {
    public ConfigInjectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfigInjectionException(String msg) {
        super(msg);
    }
}
