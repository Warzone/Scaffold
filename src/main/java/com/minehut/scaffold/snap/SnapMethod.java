package com.minehut.scaffold.snap;

import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SnapMethod implements Annotationable {
    @Getter private final SnapClass snapClass;
    @Getter private final String name;
    @Getter private final Class<?>[] argumentTypes;

    public SnapMethod(SnapClass snapClass, String name, Class<?>... argumentTypes) {
        this.snapClass = snapClass;
        this.name = name;
        this.argumentTypes = argumentTypes;
    }

    public Object get(Object instance, Object... args) throws SnapException {
        return get(Optional.of(instance), args);
    }

    public Object getStatic(Object... args) throws SnapException {
        return get(Optional.empty(), args);
    }

    @Override
    public List<Annotation> getAnnotations() {
        return Arrays.asList(getMethod().getDeclaredAnnotations());
    }

    private Object get(Optional<Object> instance, Object... args) throws SnapException {
        try {
            return getMethod().invoke(instance.orElse(null), args);
        } catch (IllegalAccessException e) {
            throw new SnapException("illegal access", e);
        } catch (ClassCastException e) {
            throw new SnapException("class cast invalid", e);
        } catch (InvocationTargetException e) {
            throw new SnapException("invocation error", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Method getMethod() throws SnapException {
        try {
            Method method = this.snapClass.getClazz().getMethod(this.name, this.argumentTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new SnapException("no such field", e);
        }
    }
}
