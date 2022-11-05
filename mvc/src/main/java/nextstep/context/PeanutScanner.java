package nextstep.context;

import java.util.Set;
import org.reflections.Reflections;

public interface PeanutScanner {
    Set<Object> scan(Reflections reflections, Set<Object> unmodifiablePeanuts);
}
