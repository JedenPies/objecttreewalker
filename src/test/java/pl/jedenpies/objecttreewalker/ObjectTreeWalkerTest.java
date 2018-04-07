package pl.jedenpies.objecttreewalker;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jedenpies.objecttreewalker.Node.randomWithName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pl.jedenpies.objecttreewalker.ObjectTreeWalker;

@RunWith(JUnit4.class)
public class ObjectTreeWalkerTest {

    @Test
    public void test_shouldIgnorePrimitiveTypesWrappersAndStrings() {
        
        // given
        Node root = randomWithName("root");
        CollectingVisitor visitor = new CollectingVisitor();
        
        // when
        new ObjectTreeWalker(visitor).walk(root);
        
        // then
        assertThat(visitor.getVisited()).hasSize(1);
        assertThat(visitor.getVisited()).containsExactly(root);
    }

    @Test
    public void test_shouldVisitNodesInArray() {

        Node root = randomWithName("root");
        Node inArray1 = randomWithName("in-array-1");
        Node inArray2 = randomWithName("in-array-2");
        root.setSomeArray(new Object[] { inArray1, inArray2 });
        CollectingVisitor visitor = new CollectingVisitor();
        
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).hasSize(3);
        assertThat(visitor.getVisited()).containsExactly(root, inArray1, inArray2);
    }
    
    @Test
    public void test_shouldVisitNodesInList() {
         
        Node root = randomWithName("root");
        Node inList1 = randomWithName("in-list-1");
        Node inList2 = randomWithName("in-list-2");
        Node inList3 = randomWithName("in-list-3");
        root.setSomeList(Arrays.asList(inList1, inList2, inList3));
        CollectingVisitor visitor = new CollectingVisitor();
        
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).hasSize(4);
        assertThat(visitor.getVisited()).containsExactly(root, inList1, inList2, inList3);
    }
    
    @Test
    public void test_shouldVisitNodesInMapValues() {
        
        Node root = randomWithName("root");
        Node inMap1 = randomWithName("1");
        Node inMap2 = randomWithName("2");
        Map<String, Object> map = new HashMap<>();
        map.put("1", inMap1);
        map.put("2", inMap2);
        root.setSomeMap(map);
        CollectingVisitor visitor = new CollectingVisitor();
        
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).containsExactly(root, inMap1, inMap2);
    }
    
    @Test
    public void test_shouldNotVisitNodesInMapKeys() {

        Node root = randomWithName("root");
        Node key1 = randomWithName("key-1");
        Node key2 = randomWithName("key-2");
        Node val1 = randomWithName("val-1");
        Node val2 = randomWithName("val-2");
        HashMap<Node, Node> map = new HashMap<>();
        map.put(key1, val1);
        map.put(key2, val2);
        root.setSomeMap(map);
        CollectingVisitor visitor = new CollectingVisitor();

        new ObjectTreeWalker(visitor).walk(root);

        assertThat(visitor.getVisited()).containsExactlyInAnyOrder(root, val1, val2);
    }

    @Test
    public void test_shouldNotVisitSameObjectInOneListTwice() {
        
        Node root = randomWithName("root");
        Node theSame = randomWithName("theSame");
        Node anotherOne = randomWithName("anotherOne");
        root.setSomeList(asList(theSame, anotherOne, theSame));
        CollectingVisitor visitor = new CollectingVisitor();
        
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).contains(root, theSame, anotherOne);
        assertThat(visitor.getVisited()).containsOnlyOnce(theSame);
    }
    
    @Test
    public void test_shouldNotVisitSameObjectTwice() {
        
        Node root = randomWithName("root");
        Node first = randomWithName("first");
        Node second = randomWithName("second");
        Node third = randomWithName("third");
        Node forth = randomWithName("forth");
        Node fifth = randomWithName("fifth");
        
        Map<String, Node> map = new HashMap<>();
        map.put("forth", forth);
        map.put("fifth", fifth);
        
        Map<String, Node> anotherMap = new HashMap<>();
        anotherMap.put("third", third);
        anotherMap.put("forth", forth);
        
        root.setInnerObject(first);
        
        first.setInnerObject(second);
        first.setSomeMap(map);
        
        second.setSomeArray(new Object[] { second, third });
        
        third.setSomeList(asList(first, second, forth));
        
        forth.setInnerObject(third);
        forth.setSomeMap(map);

        fifth.setSomeMap(anotherMap);
        fifth.setSomeList(asList(first, second));
        fifth.setInnerObject(third);
        
        CollectingVisitor visitor = new CollectingVisitor();
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).hasSize(6);
        assertThat(visitor.getVisited()).containsExactlyInAnyOrder(root, first, second, third, forth, fifth);
    }
    
    @Test
    public void test_shouldWalkIntoNestedList() {
        
        Node root = randomWithName("root");
        Node element = randomWithName("element");
        root.setSomeList(asList(asList(element), new Object()));
        
        CollectingVisitor visitor = new CollectingVisitor();
        new ObjectTreeWalker(visitor).walk(root);
        
        assertThat(visitor.getVisited()).hasSize(3);
        assertThat(visitor.getVisited()).contains(element);
    }
    
    @Test(expected = IllegalStateException.class)
    public void test_shouldThrowExceptionWhenTryingToRunTwice() {
        
        CollectingVisitor visitor = new CollectingVisitor();
        ObjectTreeWalker treeWalker = new ObjectTreeWalker(visitor);
        
        treeWalker.walk(new Object());
        treeWalker.walk(new Object());
    }
    
    @Test
    public void test_shouldIgnoreGivenType() {
        
        Node root = randomWithName("root");
        ExcludedNode excludedNode = new ExcludedNode();
        
        root.setInnerObject(excludedNode);
        root.setSomeArray(new Object[] { excludedNode });
        root.setSomeList(asList(excludedNode));
        CollectingVisitor visitor = new CollectingVisitor();
        new ObjectTreeWalker(visitor).withIgnoredType(ExcludedNode.class).walk(root);
        
        assertThat(visitor.getVisited()).doesNotContain(excludedNode);
    }
    
    @Test(expected = IllegalStateException.class)
    public void test_shouldThrowExceptionWhenTryingToConfigureAfterRun() {

        ObjectTreeWalker treeWalker = new ObjectTreeWalker(new CollectingVisitor());
        treeWalker.walk(new Object());
        treeWalker.withIgnoredType(Node.class);
    }
}
