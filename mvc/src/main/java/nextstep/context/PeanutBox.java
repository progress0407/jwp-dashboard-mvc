package nextstep.context;

import java.util.HashSet;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PeanutBox {

    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(PeanutBox.class);
    private static final ManualPeanutScanner manualPeanutScanner = ManualPeanutScanner.instance();
    private static final AutoPeanutScanner autoPeanutScanner = AutoPeanutScanner.instance();

    private final Set<Object> peanuts = new HashSet<>();

    public void init(final String path) {
        Reflections reflections = new Reflections(path);

        Set<Object> manualPeanuts = manualPeanutScanner.scan(reflections, new HashSet<>(peanuts));
        peanuts.addAll(manualPeanuts);

        Set<Object> autoPeanuts = autoPeanutScanner.scan(reflections, new HashSet<>(peanuts));
        peanuts.addAll(autoPeanuts);
    }

    public <T> T findPeanut(final Class<T> clazz) {
        return (T) peanuts.stream()
                .filter(peanut -> clazz.isAssignableFrom(peanut.getClass()))
                .findAny()
                .orElse(null);
    }

    public void clear() {
        peanuts.clear();
    }

    public void changePeanut(final Class<?> oldPeanutType, final Object newPeanut) {
        final Object beforePeanut = findPeanut(oldPeanutType);
        if (beforePeanut == null) {
            throw new RuntimeException("제거할 peanut이 존재하지 않습니다.");
        }
        peanuts.remove(beforePeanut);
        log.info("remove peanut = {}", beforePeanut.getClass().getSimpleName());
        peanuts.add(newPeanut);
        log.info("add new peanut = {}", newPeanut.getClass().getSimpleName());
    }
}
