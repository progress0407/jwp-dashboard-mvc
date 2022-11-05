package nextstep.context;

import static java.util.Arrays.stream;

import nextstep.web.annotation.GiveMePeanut;

public abstract class DiTypeClassifier implements TypeClassifier {

    /**
     * 1. public 인 기본 생성자가 반드시 있어야 하고 2. @GiveMePeanut 어노테이션이 단 한나라도 있으면 아니 된다
     */
    public static boolean isDefaultConstructorInjection(final Class<?> peanutClass) {
        return hasDefaultConstructor(peanutClass) && !hasFieldInjectionAnnotation(peanutClass);
    }

    /**
     * 1. 기본 생성자가 있다 (private 접근제한자 지원)
     * <p/>
     * 2. GiveMePeanut로 된 필드가 하나라도 있다
     */
    public static boolean isFieldInjection(final Class<?> peanutClass) {
        return hasAnyDefaultConstructor(peanutClass) && hasFieldInjectionAnnotation(peanutClass);
    }

    private static boolean hasDefaultConstructor(final Class<?> type) {
        try {
            type.getConstructor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasAnyDefaultConstructor(final Class<?> type) {
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean hasFieldInjectionAnnotation(final Class<?> type) {
        return stream(type.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(GiveMePeanut.class));
    }
}
