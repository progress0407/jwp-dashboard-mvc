package nextstep.context;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nextstep.web.annotation.PeanutConfiguration;
import nextstep.web.annotation.ThisIsPeanut;
import org.reflections.Reflections;

public class PeanutManualScanner implements PeanutScanner {

    private PeanutManualScanner() {
    }

    public static final PeanutManualScanner SINGLETON_INSTANCE = new PeanutManualScanner();

    public static final PeanutManualScanner instance() {

        return SINGLETON_INSTANCE;
    }

    public Set<Object> scan(final Reflections reflections) {

        try {
            return scanInternal(reflections);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Object> scanInternal(final Reflections reflections) throws Exception {

        Set<Object> peanuts = new HashSet<>();

        Set<Class<?>> peanutConfigClasses = reflections.getTypesAnnotatedWith(PeanutConfiguration.class);

        for (Class<?> peanutConfigClass : peanutConfigClasses) {
            List<Object> peanutObjects = constructPeanutsFromConfig(peanutConfigClass);
            peanuts.addAll(peanutObjects);
        }

        return peanuts;
    }

    private List<Object> constructPeanutsFromConfig(Class<?> peanutConfigClass) throws Exception {

        final Object peanutConfigObject = peanutConfigClass.getConstructor().newInstance();

        return stream(peanutConfigClass.getDeclaredMethods())
                .filter(peanutMethod -> peanutMethod.isAnnotationPresent(ThisIsPeanut.class))
                .map(peanutMethod -> constructPeanut(peanutConfigObject, peanutMethod))
                .collect(toList());
    }

    private Object constructPeanut(Object peanutConfigObject, Method peanutMethod) {
        try {
            return peanutMethod.invoke(peanutConfigObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
