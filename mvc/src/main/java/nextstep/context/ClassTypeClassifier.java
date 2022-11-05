package nextstep.context;

public abstract class ClassTypeClassifier implements TypeClassifier {

    /**
     * 구체 클래스이고 인터페이스가 없을 때
     */
    public static boolean isSimpleConcreteClass(final Class<?> type) {

        return !type.isInterface() && type.getInterfaces().length == 0;
    }

    public static boolean isInterface(Class<?> type) {
        return type.isInterface();
    }

    public static boolean isConcreteClassHasInterface(final Class<?> type) {
        return !type.isInterface() && type.getInterfaces().length > 0;
    }
}
