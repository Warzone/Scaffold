package com.minehut.scaffold.config;

import lombok.Getter;
import com.minehut.scaffold.config.inject.ConfigInjector;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {
    protected static final Yaml yaml = new Yaml();

    @Getter private final Map<String, Object> data;

    public Config(Map<String, Object> data) {
        this.data = data;
    }

    public Config() {
        this.data = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Config(Object object) {
        this((Map<String, Object>) object);
    }

    public Config(String raw) {
        this(yaml.load(raw));
    }

    public Config(InputStream stream) {
        this(yaml.load(stream));
    }

    public Config(Reader reader) {
        this(yaml.load(reader));
    }

    @Override
    public String toString() {
        return yaml.dump(this.data);
    }

    public void save(File file) {
        try {
            yaml.dump(this.data, new FileWriter(file));
        } catch (IOException e) {
            throw new ConfigException("unable to write to file", e);
        }
    }

    public boolean absent(String key) {
        return !exists(key);
    }

    public boolean contains(String key) {
        return exists(key);
    }

    public boolean exists(String key) {
        return this.data.containsKey(key);
    }

    public void set(Map<String, ?> values) {
        this.data.putAll(values);
    }

    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    public List<String> getStringList(String key) {
        return getList(key, String.class);
    }

    public List<Integer> getIntList(String key) {
        return getList(key, Integer.class);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> cast) {
        List list = get(key, List.class);
        for (Object object : list) {
            if (!cast.isAssignableFrom(object.getClass()))
                throw new ClassCastException("List element '" + object + "' of type " + object.getClass() + " cannot be casted to " + cast);
        }
        return (List<T>) list;
    }

    public int getInt(String key) {
        return get(key, Number.class).intValue();
    }

    public int getInt(String key, int def) {
        return contains(key) ? getInt(key) : def;
    }

    public long getLong(String key) {
        return get(key, Number.class).longValue();
    }

    public long getLong(String key, long def) {
        return contains(key) ? getLong(key) : def;
    }

    public double getDouble(String key) {
        return get(key, Number.class).doubleValue();
    }

    public double getDouble(String key, double def) {
        return contains(key) ? getDouble(key) : def;
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public String getString(String key, String def) {
        return contains(key) ? getString(key) : def;
    }

    public String getAsString(String key) {
        return contains(key) ? get(key).toString() : null;
    }

    public String getAsString(String key, String def) {
        return contains(key) ? getAsString(key) : def;
    }
    
    public boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    public boolean getBoolean(String key, boolean def) {
        return contains(key) ? getBoolean(key) : def;
    }

    @SuppressWarnings("unchecked")
    public Config getConfig(String key) {
        return get(key, Config.class);
    }

    public Object get(String key) {
        return get(key, Object.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> cast) {
        Object value = this.data.get(key);
        if (value instanceof Map)
            return (T) new Config(value);
        if (value instanceof List) {
            List list = new ArrayList<>();
            for (Object item : (List) value) {
                if (item instanceof Map)
                    list.add(new Config(item));
                else
                    list.add(item);
            }
            return (T) list;
        }
        return (T) this.data.get(key);
    }

    public ConfigInjector injector(Class<?> clazz) {
        return new ConfigInjector(clazz, this);
    }
}
