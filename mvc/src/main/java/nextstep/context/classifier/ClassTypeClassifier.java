package nextstep.context.classifier;

public abstract class ClassTypeClassifier implements TypeClassifier {

    public static boolean isSimpleConcreteClass(Class<?> clazz) {

        return isConcreteClass(clazz) && !hasInterfaces(clazz);
    }

    public static boolean isInterface(Class<?> clazz) {

        return clazz.isInterface();
    }

    public static boolean isConcreteClassImplemented(Class<?> clazz) {

        return isConcreteClass(clazz) && hasInterfaces(clazz);
    }

    private static boolean isConcreteClass(Class<?> clazz) {

        return !clazz.isInterface();
    }

    private static boolean hasInterfaces(Class<?> type) {

        return type.getInterfaces().length > 0;
    }
}
