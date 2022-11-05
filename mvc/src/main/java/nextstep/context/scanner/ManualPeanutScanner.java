package nextstep.context.scanner;

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

public class ManualPeanutScanner implements PeanutScanner {

    private ManualPeanutScanner() {
    }

    private static final ManualPeanutScanner SINGLETON_INSTANCE = new ManualPeanutScanner();

    public static ManualPeanutScanner instance() {

        return SINGLETON_INSTANCE;
    }

    @Override
    public Set<Object> scan(Reflections reflections, Set<Object> unmodifiablePeanuts) {

        try {
            return scanInternal(reflections);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Object> scanInternal(Reflections reflections) throws Exception {

        Set<Object> peanuts = new HashSet<>();

        Set<Class<?>> peanutConfigClasses = reflections.getTypesAnnotatedWith(PeanutConfiguration.class);

        for (Class<?> peanutConfigClass : peanutConfigClasses) {
            List<Object> peanutObjects = createPeanutsFromConfig(peanutConfigClass);
            peanuts.addAll(peanutObjects);
        }

        return peanuts;
    }

    private List<Object> createPeanutsFromConfig(Class<?> peanutConfigClass) throws Exception {

        Object peanutConfigObject = peanutConfigClass.getConstructor().newInstance();

        return stream(peanutConfigClass.getDeclaredMethods())
                .filter(peanutMethod -> peanutMethod.isAnnotationPresent(ThisIsPeanut.class))
                .map(peanutMethod -> createPeanut(peanutConfigObject, peanutMethod))
                .collect(toList());
    }

    private Object createPeanut(Object peanutConfigObject, Method peanutMethod) {

        try {
            return peanutMethod.invoke(peanutConfigObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
