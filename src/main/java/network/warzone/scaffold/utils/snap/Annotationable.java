package network.warzone.scaffold.utils.snap;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public interface Annotationable {
    List<Annotation> getAnnotations();

    default boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType).isPresent();
    }

    @SuppressWarnings("unchecked")
    default <T extends Annotation> Optional<T> getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : getAnnotations())
            if (annotation.annotationType() == annotationType)
                return Optional.of((T) annotation);
        return Optional.empty();
    }
}
