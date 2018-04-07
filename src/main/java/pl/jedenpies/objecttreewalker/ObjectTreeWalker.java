package pl.jedenpies.objecttreewalker;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static pl.jedenpies.objecttreewalker.ReflectionUtils.allFields;
import static pl.jedenpies.objecttreewalker.ReflectionUtils.isArray;
import static pl.jedenpies.objecttreewalker.ReflectionUtils.isCollection;
import static pl.jedenpies.objecttreewalker.ReflectionUtils.isPrimitiveArray;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ObjectTreeWalker {

    private Queue<WrappedObject> noToVisit = new LinkedList<>();
    private Queue<WrappedObject> toVisit = new LinkedList<>();
    private Set<Class<?>> ignoredTypes = new HashSet<>();
    private boolean run = false;

    private Visitor visitor;

    public ObjectTreeWalker(Visitor visitor) {
        requireNonNull(visitor);
        addDefaultIgnoredTypes();
        this.visitor = visitor;
    }

    public ObjectTreeWalker withIgnoredType(Class<?> clazz) {
        requireNonNull(clazz);
        ensureNotRunAlready();
        ignoredTypes.add(clazz);
        return this;
    }

    public void walk(Object root) {
        requireNonNull(root);
        markRun();
        planVisitIfApplies(root);
        startVisiting();
    }

    private void addDefaultIgnoredTypes() {
        ignoredTypes.add(String.class);
        ignoredTypes.add(Number.class);
        ignoredTypes.add(Boolean.class);
    }

    synchronized private void markRun() {
        ensureNotRunAlready();
        run = true;
    }

    private void ensureNotRunAlready() {
        if (run) throw new IllegalStateException();
    }

    private void startVisiting() {
        while (!toVisit.isEmpty()) {
            WrappedObject nextElement = toVisit.poll();
            visitor.visit(nextElement.getObject());
            noToVisit.add(nextElement);
            planVisitsForFieldsOf(nextElement.getObject());
        }
    }

    private void planVisitsForFieldsOf(Object object) {
        allFields(object.getClass())
                .stream()
                .forEach(f -> planVisitForField(f, object));
    }

    private void planVisitForField(Field field, Object object) {

        field.setAccessible(true);
        try {
            Object value = field.get(object);
            planVisitIfApplies(value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // We can't do anything here, just ignore it
        } finally {
            field.setAccessible(false);
        }
    }

    private void planVisitIfApplies(Object object) {

        if (object == null || object.getClass().isPrimitive() || isIgnoredType(object.getClass())) return;

        WrappedObject wrappedObject = new WrappedObject(object);
        if (wasVisitedOrIsPlannedTo(wrappedObject)) return;
        if (isPrimitiveArray(object)) noToVisit.add(wrappedObject);
        else if (isArray(object)) planVisitsForArrayElements(object, wrappedObject);
        else if (isCollection(object)) planVisitsForCollectionElements(object, wrappedObject);
        else if (ReflectionUtils.isMap(object)) planVisitsForMapValues(object, wrappedObject);
        else toVisit.add(wrappedObject);
    }

    private void planVisitsForMapValues(Object object, WrappedObject visitedObject) {
        Map<?, ?> map = (Map<?, ?>) object;
        planVisitsForCollectionElements(map.values(), visitedObject);
    }

    private void planVisitsForArrayElements(Object object, WrappedObject visitedObject) {
        Object[] elements = (Object[]) object;
        planVisitsForCollectionElements(asList(elements), visitedObject);
    }

    private void planVisitsForCollectionElements(Object object, WrappedObject visitedObject) {
        noToVisit.add(visitedObject);
        Collection<?> collection = (Collection<?>) object;
        collection.stream().forEach(this::planVisitIfApplies);
    }

    private boolean wasVisitedOrIsPlannedTo(WrappedObject visitedObject) {
        return noToVisit.contains(visitedObject) || toVisit.contains(visitedObject);
    }

    private boolean isIgnoredType(Class<? extends Object> clazz) {
        return ignoredTypes.stream().filter(t -> t.isAssignableFrom(clazz)).findAny().isPresent();
    }

    /**
     * We need this to avoid custom equals() implementations
     * @author Patryk Dobrowolski
     *
     */
    private static class WrappedObject {

        private Object wrapped;
        
        private WrappedObject(Object wrapped) {
            requireNonNull(wrapped);
            this.wrapped = wrapped;
        }
    
        private Object getObject() {
            return wrapped;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || !(object instanceof WrappedObject)) return false;
            WrappedObject that = (WrappedObject) object;
            return this.wrapped == that.wrapped;
        }
        
        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }
    }
}
