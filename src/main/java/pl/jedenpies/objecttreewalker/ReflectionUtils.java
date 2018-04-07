package pl.jedenpies.objecttreewalker;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class ReflectionUtils {

    private ReflectionUtils() {}

    static boolean isPrimitiveArray(Object object) {
        return object.getClass().isArray() && object.getClass().getComponentType().isPrimitive();
    }

    static List<Field> allFields(Class<?> clazz) {
        if (Object.class.equals(clazz)) return emptyList();
        List<Field> fields = new LinkedList<>();
        fields.addAll(asList(clazz.getDeclaredFields()));
        fields.addAll(allFields(clazz.getSuperclass()));
        return fields;
    }

    static boolean isCollection(Object object) {
        return object instanceof Collection;
    }

    static boolean isMap(Object object) {
        return object instanceof Map;
    }

    static boolean isArray(Object object) {
        return object.getClass().isArray();
    }
}
