package pl.jedenpies.objecttreewalker;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("unused")
public class Node {

    private String name;
    private String someString;
    private Integer someInteger;
    private double someDouble;
    private Object innerObject;
    private List<Object> someList;
    private Set<Object> someSet;
    private Map<?, ?> someMap;
    private Object[] someArray;
    
    public static Node randomWithName(String name) {
        Node result = new Node(name);
        result.someString = Long.valueOf(new Random().nextLong()).toString();
        result.someInteger = new Random().nextInt();
        result.someDouble = new Random().nextDouble();
        return result;
    }
    
    protected Node(String name) {
        this.name = name;
    }

    public void setSomeString(String someString) {
        this.someString = someString;
    }
    
    public void setSomeInteger(Integer someInteger) {
        this.someInteger = someInteger;
    }
    
    public void setInnerObject(Object innerObject) {
        this.innerObject = innerObject;
    }
    
    public void setSomeList(List<Object> someList) {
        this.someList = someList;
    }
    
    public void setSomeSet(Set<Object> someSet) {
        this.someSet = someSet;
    }
    
    public void setSomeMap(Map<?, ?> someMap) {
        this.someMap = someMap;
    }
    
    public void setSomeArray(Object[] someArray) {
        this.someArray = someArray;
    }
}
