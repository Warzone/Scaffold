package com.minehut.scaffold.config.inject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ConfigPath {
    /**
     * The path to the config section.
     * @return The path, separated by periods.
     */
    String value();
}
