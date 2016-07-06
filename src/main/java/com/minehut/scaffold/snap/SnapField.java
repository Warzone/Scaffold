package com.minehut.scaffold.snap;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SnapField implements Annotationable {
    @Getter private final SnapClass snapClass;
    @Getter private final String name;

    public SnapField(SnapClass snapClass, String name) {
        this.snapClass = snapClass;
        this.name = name;
    }

    public void set(Object instance, Object value) throws SnapException {
        set(Optional.of(instance), value);
    }

    public void setStatic(Object value) throws SnapException {
        set(Optional.empty(), value);
    }

    public Object get(Object instance) throws SnapException {
        return get(Optional.of(instance));
    }

    public Object getStatic() throws SnapException {
        return get(Optional.empty());
    }

    @SuppressWarnings("unchecked")
    public Class getFieldType() {
        return getField().getType();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return Arrays.asList(getField().getDeclaredAnnotations());
    }

    private void set(Optional<Object> instance, Object value) throws SnapException {
        Field field = getField();
        try {
            field.set(instance.orElse(null), value);
        } catch (IllegalArgumentException e) {
            throw new SnapException("Illegal argument: '" + this.name + "'.", e);
        } catch (IllegalAccessException e) {
            throw new SnapException("Illegal access: '" + this.name + "'.", e);
        }
    }

    private Object get(Optional<Object> instance) throws SnapException {
        try {
            return getField().get(instance.orElse(null));
        } catch (IllegalAccessException e) {
            throw new SnapException("Illegal access: '" + this.name + "'.", e);
        } catch (ClassCastException e) {
            throw new SnapException("Class cast invalid: '" + this.name + "'.", e);
        }
    }

    private Field getField() throws SnapException {
        try {
            Field field = this.snapClass.getClazz().getDeclaredField(this.name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new SnapException("No such field: '" + this.name + "'.", e);
        }
    }

    public SnapClass getSnapClass() {
        return snapClass;
    }

    public String getName() {
        return name;
    }
}
