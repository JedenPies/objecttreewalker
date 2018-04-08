package pl.jedenpies.objecttreewalker;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

public class ComposedVisitor implements Visitor {

    private Map<Class<?>, Visitor> visitors = new HashMap<>();
    private Visitor defaultVisitor = t -> {};
    
    public ComposedVisitor withDefaultVisitor(Visitor defaultVisitor) {
        requireNonNull(defaultVisitor);
        this.defaultVisitor = defaultVisitor;
        return this;
    }
    
    public ComposedVisitor withVisitor(Class<?> clazz, Visitor visitor) {
        requireNonNull(clazz);
        requireNonNull(visitor);
        visitors.put(clazz, visitor);
        return this;
    }
    
    @Override
    public void visit(Object object) {
        visitors
            .getOrDefault(object.getClass(), defaultVisitor)
            .visit(object);
    }
}
