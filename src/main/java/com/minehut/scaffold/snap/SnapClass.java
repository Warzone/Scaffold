package com.minehut.scaffold.snap;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnapClass implements Annotationable {
    @Getter private final Class<?> clazz;

    public SnapClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public SnapClass(String forName) {
        try {
            this.clazz = Class.forName(forName);
        } catch (ClassNotFoundException e) {
            throw new SnapException("class not found", e);
        }
    }

    public SnapConstructor getConstructor(Class<?>... argumentTypes)  {
        return new SnapConstructor(this, argumentTypes);
    }

    public SnapMethod getMethod(String name, Class<?>... argumentTypes) {
        return new SnapMethod(this, name, argumentTypes);
    }

    public SnapField getField(String name) {
        return new SnapField(this, name);
    }

    public List<SnapField> getFields() {
        return getFields(this.clazz.getDeclaredFields());
    }

    public List<SnapField> getPublicFields() {
        return getFields(this.clazz.getFields());
    }

    public List<SnapClass> getClasses() {
        return getClasses(this.clazz.getDeclaredClasses());
    }

    public List<SnapClass> getPublicClasses() {
        return getClasses(this.clazz.getClasses());
    }

    @Override
    public List<Annotation> getAnnotations() {
        return Arrays.asList(this.clazz.getDeclaredAnnotations());
    }

    private List<SnapClass> getClasses(Class[] classesArray) {
        List<SnapClass> classes = new ArrayList<>();
        for (Class<?> clazz : classesArray)
            classes.add(new SnapClass(clazz));
        return classes;
    }

    private List<SnapField> getFields(Field[] fieldsArray) {
        List<SnapField> fields = new ArrayList<>();
        for (Field field : fieldsArray)
            fields.add(new SnapField(this, field.getName()));
        return fields;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
