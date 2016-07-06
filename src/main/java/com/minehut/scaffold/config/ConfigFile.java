package com.minehut.scaffold.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class ConfigFile extends Config {
    private final File file;

    @SuppressWarnings("unchecked")
    public ConfigFile(File file) {
        this.file = file;
        try {
            Map map = yaml.loadAs(new FileReader(file), Map.class);
            set(map);
        } catch (FileNotFoundException e) {
            throw new ConfigException("failed to read config", e);
        }
    }

    public void save() {
        save(this.file);
    }
}
