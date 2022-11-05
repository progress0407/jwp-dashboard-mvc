package nextstep.context;

import static nextstep.context.ClassTypeClassifier.isConcreteClassHasInterface;
import static nextstep.context.ClassTypeClassifier.isInterface;
import static nextstep.context.ClassTypeClassifier.isSimpleConcreteClass;
import static nextstep.context.DiTypeClassifier.isDefaultConstructorInjection;
import static nextstep.context.DiTypeClassifier.isFieldInjection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import nextstep.web.annotation.Controller;
import nextstep.web.annotation.GiveMePeanut;
import nextstep.web.annotation.ImPeanut;
import nextstep.web.annotation.Repository;
import nextstep.web.annotation.Service;
import org.reflections.Reflections;

public class AutoPeanutScanner implements PeanutScanner {

    private Set<Object> peanuts;
    private Reflections reflections;

    private AutoPeanutScanner() {
    }

    public static final AutoPeanutScanner SINGLETON_INSTANCE = new AutoPeanutScanner();

    public static AutoPeanutScanner instance() {

        return SINGLETON_INSTANCE;
    }

    @Override
    public Set<Object> scan(Reflections reflections, Set<Object> peanuts) {

        this.peanuts = peanuts;
        this.reflections = reflections;

        try {
            scanInternal();
            return this.peanuts;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanInternal() throws Exception {

        Set<Class<?>> peanutClasses = findPeanutAnnotatedTypes(reflections);

        for (Class<?> peanutType : peanutClasses) {
            if (isNotCreationCase(peanutType)) {
                continue;
            }
            final Object newInstance = dfsClassType(peanutType);
            peanuts.add(newInstance);
        }
    }

    private Object dfsClassType(final Class<?> peanutClass) throws Exception {

        // 구체 클래스 O & 인터페이스 X
        if (isSimpleConcreteClass(peanutClass)) {
            return dfsDiType(peanutClass);
        }

        // 구체 클래스 O & 인터페이스 O
        else if (isConcreteClassHasInterface(peanutClass)) {
            return dfsDiType(peanutClass);
        }

        // 구체 클래스 X
        else if (isInterface(peanutClass)) {
            return dfsDiType(subType(peanutClass));
        }

        throw new RuntimeException("예측하지 못한 타입입니다: + " + peanutClass);
    }

    private Set<Class<?>> findPeanutAnnotatedTypes(final Reflections reflections) {
        final Set<Class<?>> peanuts = reflections.getTypesAnnotatedWith(ImPeanut.class);
        peanuts.addAll(reflections.getTypesAnnotatedWith(Controller.class));
        peanuts.addAll(reflections.getTypesAnnotatedWith(Service.class));
        peanuts.addAll(reflections.getTypesAnnotatedWith(Repository.class));
        return peanuts;
    }

    /**
     * peanutClass가
     * <p/>
     * 어노테이션 자체이거나
     * <p/>
     * 인터페이스 자체라면(#1)
     * <p/>
     * 처리하지 않는다
     *
     * #1의 경우 인터페이스에 따른 생성방식을 따로 처리하고 있는 로직이 있기 때문에 이곳에서 처리하지 않는다
     */
    private boolean isNotCreationCase(final Class<?> peanutClass) {
        return peanutClass.isAnnotation() && peanutClass.getInterfaces().length > 0;
    }

    /**
     * 하위 클래스를 가져옵니다.
     * <p/>
     * 단, 하위 클래스가 하나인 경우만을 가정합니다.
     * <p/>
     * 즉, 스프링의 @Praimary를 고려하지 않았습니다.
     */
    private Class<?> subType(final Class<?> peanutClass) {

        final Object[] subPeanutClasses = reflections.getSubTypesOf(peanutClass).toArray();

        if (subPeanutClasses.length == 0) {
            throw new RuntimeException("자식 클래스가 존재하지 않습니다. : " + peanutClass.toString());
        }

        if (subPeanutClasses.length >= 2) {
            throw new RuntimeException("여러 하위 타입을 지원하지 않습니다. : " + Arrays.toString(subPeanutClasses));
        }

        return (Class<?>) subPeanutClasses[0];
    }

    private Object dfsDiType(final Class<?> peanutClass) throws Exception {

        if (isAlreadyExistPeanut(peanutClass)) {
            return findPeanut(peanutClass);
        }
        // 생성자 주입:  기본 생성자
        else if (isDefaultConstructorInjection(peanutClass)) {
            return createByDefaultConstructor(peanutClass);
        }
        // 필드 주입
        else if (isFieldInjection(peanutClass)) {
            return createByFileInjection(peanutClass);
        }
        // 생성자 주입:  1개 이상의 인자를 가진 생성자
        else {
            return createByConstructorWithArguments(peanutClass);
        }
    }

    private Object createByDefaultConstructor(Class<?> peanutClass) throws Exception {
        final Constructor<?> defaultConstructor = getDefaultConstructor(peanutClass);
        return defaultConstructor.newInstance();
    }

    private Object createByFileInjection(Class<?> peanutClass) throws Exception {
        final Constructor<?> hiddenDefaultConstructor = findHiddenDefaultConstructor(peanutClass);
        final Object newObject = hiddenDefaultConstructor.newInstance();
        final Field[] fields = peanutClass.getDeclaredFields();
        for (final Field field : fields) {
            if (field.isAnnotationPresent(GiveMePeanut.class)) {
                field.setAccessible(true);
                field.set(newObject, dfsClassType(field.getType()));
            }
        }
        return newObject;
    }

    private Object createByConstructorWithArguments(Class<?> peanutClass) throws Exception {
        final Constructor<?>[] constructors = peanutClass.getDeclaredConstructors();
        validateConstructorIsUnique(constructors);
        final Constructor<?> constructor = constructors[0];
        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Object[] parameterInstances = findAndSaveConstructorArguments(parameterTypes);
        return constructor.newInstance(parameterInstances);
    }

    private Object[] findAndSaveConstructorArguments(final Class<?>[] argumentClasses) throws Exception {
        final Object[] argumentObjects = new Object[argumentClasses.length];

        for (int i = 0; i < argumentClasses.length; i++) {
            final Class<?> argumentClass = argumentClasses[i];
            final Object argumentObject = dfsClassType(argumentClass);
            this.peanuts.add(argumentObject);
            argumentObjects[i] = argumentObject;
        }

        return argumentObjects;
    }

    private void validateConstructorIsUnique(final Constructor<?>[] constructors) {
        if (constructors.length > 1) {
            throw new RuntimeException("Peanut은 하나의 생성자만을 가져야 합니다.");
        }
    }

    private Constructor<?> findHiddenDefaultConstructor(final Class<?> type) throws NoSuchMethodException {
        final Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor;
    }

    private Constructor<?> getDefaultConstructor(final Class<?> peanutType) {
        try {
            return peanutType.getConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isAlreadyExistPeanut(final Class<?> peanut) {
        return findPeanut(peanut) != null;
    }
    private <T> T findPeanut(final Class<T> clazz) {
        return (T) peanuts.stream()
                .filter(peanut -> clazz.isAssignableFrom(peanut.getClass()))
                .findAny()
                .orElse(null);
    }
}
