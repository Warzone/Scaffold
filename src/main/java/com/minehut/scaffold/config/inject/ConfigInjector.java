package com.minehut.scaffold.config.inject;

import com.minehut.scaffold.config.Config;
import com.minehut.scaffold.snap.Annotationable;
import com.minehut.scaffold.snap.SnapClass;
import com.minehut.scaffold.snap.SnapException;
import com.minehut.scaffold.snap.SnapField;

import java.util.Optional;

public class ConfigInjector {
    private final Class<?> clazz;
    private final Config section;

    public ConfigInjector(Class<?> clazz, Config section) {
        this.clazz = clazz;
        this.section = section;
    }

    public void inject() throws ConfigInjectionException {
        inject(new SnapClass(this.clazz), Optional.of(this.section));
    }

    @SuppressWarnings("unchecked")
    private void inject(SnapClass snap, Optional<Config> section) throws ConfigInjectionException {
        for (SnapField field : snap.getFields()) {
            if (!field.hasAnnotation(ConfigKey.class))
                continue;

            ConfigKey info = field.getAnnotation(ConfigKey.class).get();
            String key = info.key().length() > 0 ? info.key() : field.getName();

            Optional<Config> nested = navigateToPath(section, field);
            boolean exists = !nested.isPresent() || nested.get().contains(key);
            boolean required = field.getFieldType() != Optional.class;

            if (required && !exists)
                throw new ConfigInjectionException("Missing required key: '" + key + "'.");
            else if (!exists)
                continue;

            Object value = nested.get().get(key);

            if (!required)
                value = Optional.ofNullable(value);

            try {
                field.setStatic(value);
            } catch (SnapException e) {
                throw new ConfigInjectionException("Unable to set key: '" + key + "'.", e);
            }
        }

        for (SnapClass nested : snap.getClasses()) {
            Optional<Config> path = navigateToPath(section, nested);
            inject(nested, path);
        }
    }

    private Optional<Config> navigateToPath(Optional<Config> parent, Annotationable object) {
        if (!object.hasAnnotation(ConfigPath.class))
            return parent;
        if (!parent.isPresent())
            return parent;

        String rawPath = object.getAnnotation(ConfigPath.class).get().value();
        String[] path = rawPath.split("\\.");
        Config result = parent.get();

        for (String section : path) {
            if (result.contains(section)) {
                try {
                    result = result.getConfig(section);
                } catch (ClassCastException e) {
                    throw new ConfigInjectionException("Invalid type for configuration path: '" + rawPath + "'.", e);
                }
            }
            else
                return Optional.empty();
        }

        return Optional.of(result);
    }
}
