package nextstep.context.classifier;

import static java.util.Arrays.stream;

import java.lang.reflect.Constructor;
import nextstep.web.annotation.GiveMePeanut;

public abstract class DiTypeClassifier implements TypeClassifier {

    public static boolean isDefaultConstructorInjection(Class<?> peanutClass) {
        
        return hasDefaultConstructor(peanutClass) && !hasFieldInjectionAnnotation(peanutClass);
    }

    public static boolean isFieldInjection(Class<?> peanutClass) {
        
        return hasDefaultConstructor(peanutClass) && hasFieldInjectionAnnotation(peanutClass);
    }

    public static boolean isConstructorWithArgumentsInjection(Class<?> peanutClass) {

        return hasConstructorWithArguments(peanutClass) && !hasFieldInjectionAnnotation(peanutClass);
    }

    private static boolean hasDefaultConstructor(Class<?> type) {
        
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasFieldInjectionAnnotation(Class<?> type) {
        
        return stream(type.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(GiveMePeanut.class));
    }

    private static boolean hasConstructorWithArguments(Class<?> peanutClass) {

        Constructor<?> constructor = peanutClass.getDeclaredConstructors()[0];
        Class<?>[] parameterClasses = constructor.getParameterTypes();

        return parameterClasses.length >= 1;
    }
}
