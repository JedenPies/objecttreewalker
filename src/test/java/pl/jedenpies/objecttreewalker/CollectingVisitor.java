package pl.jedenpies.objecttreewalker;

import static java.util.Collections.unmodifiableList;

import java.util.LinkedList;
import java.util.List;

import pl.jedenpies.objecttreewalker.Visitor;

/**
 * Collects all visits
 * @author Patryk Dobrowolski
 *
 */
public class CollectingVisitor implements Visitor {

    private List<Object> visited = new LinkedList<>();

    @Override
    public void visit(Object object) {
        visited.add(object);
        
    }

    public List<Object> getVisited() {
        return unmodifiableList(visited);
    }
}
