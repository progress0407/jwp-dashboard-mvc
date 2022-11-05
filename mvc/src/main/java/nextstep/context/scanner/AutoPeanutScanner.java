package nextstep.context.scanner;

import static nextstep.context.classifier.ClassTypeClassifier.isConcreteClassImplemented;
import static nextstep.context.classifier.ClassTypeClassifier.isInterface;
import static nextstep.context.classifier.ClassTypeClassifier.isSimpleConcreteClass;
import static nextstep.context.classifier.DiTypeClassifier.isConstructorWithArgumentsInjection;
import static nextstep.context.classifier.DiTypeClassifier.isDefaultConstructorInjection;
import static nextstep.context.classifier.DiTypeClassifier.isFieldInjection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private static final AutoPeanutScanner SINGLETON_INSTANCE = new AutoPeanutScanner();

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

        for (Class<?> peanutClass : peanutClasses) {
            if (isNotCreationCase(peanutClass)) {
                continue;
            }
            validateConstructorUnique(peanutClass);
            Object newInstance = createPeanutsByClassTypeRecursively(peanutClass);
            peanuts.add(newInstance);
        }
    }

    /**
     * 등록하려는 Peanut이 구체 클래스인지, 인터페이스를 가지고 있는지 여부에 따라 다르게 동작하는 메서드입니다.
     * <p/>
     * DFS로 동작합니다.
     */
    private Object createPeanutsByClassTypeRecursively(Class<?> peanutClass) throws Exception {

        if (isSimpleConcreteClass(peanutClass)) {
            return createPeanutsByInjectionTypeRecursively(peanutClass);
        }

        else if (isConcreteClassImplemented(peanutClass)) {
            return createPeanutsByInjectionTypeRecursively(peanutClass);
        }

        else if (isInterface(peanutClass)) {
            return createPeanutsByInjectionTypeRecursively(subType(peanutClass));
        }

        throw new RuntimeException("예측하지 못한 타입입니다: + " + peanutClass);
    }

    /**
     * Peanut을 생성하거나 이미 존재하는 Peanut을 반환합니다.
     * <p/>
     * DI의 형태(생성자, 필드)에 따라 생성방식이 다릅니다.
     * <p/>
     * DFS로 동작합니다.
     */
    private Object createPeanutsByInjectionTypeRecursively(Class<?> peanutClass) throws Exception {

        if (isAlreadyExistPeanut(peanutClass)) {
            return findPeanut(peanutClass);
        }

        else if (isDefaultConstructorInjection(peanutClass)) {
            return createPeanutByDefaultConstructor(peanutClass);
        }

        else if (isConstructorWithArgumentsInjection(peanutClass)) {
            return createPeanutByConstructorWithArguments(peanutClass);
        }

        else if (isFieldInjection(peanutClass)) {
            return createPeanutByFileInjection(peanutClass);
        }

        throw new RuntimeException("허용하지 않는 DI 방식입니다. : + " + peanutClass);
    }

    private Set<Class<?>> findPeanutAnnotatedTypes(Reflections reflections) {

        Set<Class<?>> peanuts = reflections.getTypesAnnotatedWith(ImPeanut.class);
        peanuts.addAll(reflections.getTypesAnnotatedWith(Controller.class));
        peanuts.addAll(reflections.getTypesAnnotatedWith(Service.class));
        peanuts.addAll(reflections.getTypesAnnotatedWith(Repository.class));

        return peanuts;
    }

    /**
     * peanutClass가 어노테이션이거나 인터페이스 구현체라면 생성하지 않습니다.
     */
    private boolean isNotCreationCase(Class<?> peanutClass) {
        
        return peanutClass.isAnnotation() || isConcreteClassImplemented(peanutClass);
    }

    /**
     * 하위 클래스를 가져옵니다.
     * <p/>
     * 단, 하위 클래스가 하나인 경우만을 가정합니다.
     * <p/>
     * 즉, 스프링의 @Praimary를 고려하지 않습니다.
     */
    private Class<?> subType(Class<?> peanutClass) {

        Object[] subPeanutClasses = reflections.getSubTypesOf(peanutClass).toArray();

        if (subPeanutClasses.length == 0) {
            throw new RuntimeException("자식 클래스가 존재하지 않습니다. : " + peanutClass.toString());
        }

        if (subPeanutClasses.length >= 2) {
            throw new RuntimeException("여러 하위 타입을 지원하지 않습니다. : " + Arrays.toString(subPeanutClasses));
        }

        return (Class<?>) subPeanutClasses[0];
    }

    private Object createPeanutByDefaultConstructor(Class<?> peanutClass) throws Exception {

        return getDefaultConstructor(peanutClass).newInstance();
    }

    /**
     * 1. 필드에 들어가는 peanut 을 만들어서 autoPeanutScanner의 peanuts 필드에 추가합니다.
     * 2. 생성될 peanut의 필드에 주입을 합니다.
     * 3. 필드 주입한 peanut을 생성하여 반환합니다.
     */
    private Object createPeanutByFileInjection(Class<?> peanutClass) throws Exception {

        Constructor<?> hiddenDefaultConstructor = getDefaultConstructor(peanutClass);
        Object newObject = hiddenDefaultConstructor.newInstance();
        Field[] fields = peanutClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(GiveMePeanut.class)) {
                field.setAccessible(true);
                Object fieldObject = createPeanutsByClassTypeRecursively(field.getType());
                field.set(newObject, fieldObject);
                this.peanuts.add(fieldObject);
            }
        }
        return newObject;
    }

    private Object createPeanutByConstructorWithArguments(Class<?> peanutClass) throws Exception {

        Constructor<?>[] constructors = peanutClass.getDeclaredConstructors();
        Constructor<?> constructor = constructors[0];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameterObject = findAndSaveConstructorArguments(parameterTypes);
        
        return constructor.newInstance(parameterObject);
    }

    /**
     * 1. peanut인자를 생성하거나 가져와서 필드 배열에 담습니다.
     * <p/>
     * 2. 가져온 peanut인자를 autoPeanutScanner의 peanuts 필드에 추가합니다.
     */
    private Object[] findAndSaveConstructorArguments(Class<?>[] argumentClasses) throws Exception {

        Object[] argumentObjects = new Object[argumentClasses.length];

        for (int i = 0; i < argumentClasses.length; i++) {
            Class<?> argumentClass = argumentClasses[i];
            Object argumentObject = createPeanutsByClassTypeRecursively(argumentClass);
            argumentObjects[i] = argumentObject;
            this.peanuts.add(argumentObject);
        }

        return argumentObjects;
    }

    private void validateConstructorUnique(Class<?> peanutClass) {

        if (peanutClass.getDeclaredConstructors().length >= 2) {
            throw new RuntimeException("Peanut은 하나의 생성자만을 가져야 합니다.");
        }
    }

    private Constructor<?> getDefaultConstructor(Class<?> peanutClass) throws Exception {

        Constructor<?> constructor = peanutClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor;
    }

    private boolean isAlreadyExistPeanut(Class<?> peanut) {

        return findPeanut(peanut) != null;
    }

    private <T> T findPeanut(Class<T> clazz) {

        return (T) peanuts.stream()
                .filter(peanut -> clazz.isAssignableFrom(peanut.getClass()))
                .findAny()
                .orElse(null);
    }
}
