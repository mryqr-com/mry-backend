package com.mryqr.core.common.domain.idnode;

import com.mryqr.core.common.domain.idnode.exception.IdNodeLevelOverflowException;
import com.mryqr.core.common.domain.idnode.exception.IdNodeNotFoundException;
import com.mryqr.core.common.domain.idnode.exception.NodeIdFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdTreeTest {

    @Test
    public void should_create_tree() {
        new IdTree(new ArrayList<>(0));
        IdTree idTree = new IdTree("111");
        assertEquals(0, idTree.nodeById("111").getChildren().size());
    }

    @Test
    public void should_fail_create_tree_if_id_contain_separator() {
        Assertions.assertThrows(NodeIdFormatException.class, () -> {
            new IdTree("1/11");
        });
    }

    @Test
    public void should_add_node() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode("222", "444");
        assertEquals(2, idTree.nodeById("111").getChildren().size());
        assertEquals(1, idTree.nodeById("222").getChildren().size());
        assertEquals(0, idTree.nodeById("333").getChildren().size());
        assertEquals(0, idTree.nodeById("444").getChildren().size());
    }

    @Test
    public void should_equal() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode("222", "444");

        IdTree idTree2 = new IdTree("111");
        idTree2.addNode("111", "222");
        idTree2.addNode("111", "333");
        idTree2.addNode("222", "444");
        assertEquals(idTree, idTree2);
    }

    @Test
    public void should_fail_add_node_if_id_contains_separator() {
        Assertions.assertThrows(NodeIdFormatException.class, () -> {
            IdTree idTree = new IdTree("111");
            idTree.addNode("111", "2/22");
        });
    }

    @Test
    public void should_fail_add_node_if_parent_node_not_found() {
        Assertions.assertThrows(IdNodeNotFoundException.class, () -> {
            IdTree idTree = new IdTree("111");
            idTree.addNode("aaa", "222");
        });
    }

    @Test
    public void should_build_schema() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode("222", "444");
        idTree.addNode(null, "555");
        IdTreeHierarchy hierarchy = idTree.buildHierarchy(3);
        assertEquals(5, hierarchy.allIds().size());
        assertEquals("111", hierarchy.schemaOf("111"));
        assertEquals("111/222", hierarchy.schemaOf("222"));
        assertEquals("111/333", hierarchy.schemaOf("333"));
        assertEquals("111/222/444", hierarchy.schemaOf("444"));
        assertEquals("555", hierarchy.schemaOf("555"));
    }

    @Test
    public void should_fail_build_schema_if_max_allowed_level_reached() {
        Assertions.assertThrows(IdNodeLevelOverflowException.class, () -> {
            IdTree idTree = new IdTree("111");
            idTree.addNode("111", "222");
            idTree.addNode("111", "333");
            idTree.addNode("222", "444");
            idTree.addNode(null, "555");
            idTree.buildHierarchy(2);
        });
    }

    @Test
    public void should_remove_node() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode("222", "444");
        idTree.addNode(null, "555");

        idTree.removeNode("555");
        assertFalse(idTree.buildHierarchy(5).containsId("555"));

        boolean result = idTree.removeNode("222");
        assertTrue(result);
        assertFalse(idTree.buildHierarchy(5).containsId("222"));
        assertFalse(idTree.buildHierarchy(5).containsId("444"));
        assertTrue(idTree.buildHierarchy(5).containsId("111"));
        assertTrue(idTree.buildHierarchy(5).containsId("333"));
        assertEquals(2, idTree.buildHierarchy(5).allIds().size());
    }

    @Test
    public void should_silently_remove_none_exist_node() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode("222", "444");
        idTree.addNode(null, "555");

        idTree.removeNode("whatever");
        IdTree idTree2 = new IdTree("111");
        idTree2.addNode("111", "222");
        idTree2.addNode("111", "333");
        idTree2.addNode("222", "444");
        idTree2.addNode(null, "555");
        assertEquals(idTree, idTree2);
    }

    @Test
    public void should_merge() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");

        IdTree idTree2 = new IdTree("aaa");
        idTree2.addNode("aaa", "bbb");

        idTree.merge(idTree2);
        IdTreeHierarchy hierarchy = idTree.buildHierarchy(5);
        assertEquals("111", hierarchy.schemaOf("111"));
        assertEquals("111/222", hierarchy.schemaOf("222"));
        assertEquals("aaa", hierarchy.schemaOf("aaa"));
        assertEquals("aaa/bbb", hierarchy.schemaOf("bbb"));
    }

    @Test
    public void should_merge_and_self_as_victim_if_id_duplicates() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");

        IdTree idTree2 = new IdTree("aaa");
        idTree2.addNode("aaa", "bbb");
        idTree2.addNode("bbb", "222");
        idTree.merge(idTree2);
        IdTreeHierarchy hierarchy = idTree.buildHierarchy(5);
        assertEquals("111", hierarchy.schemaOf("111"));
        assertEquals("aaa/bbb/222", hierarchy.schemaOf("222"));
        assertEquals("aaa", hierarchy.schemaOf("aaa"));
        assertEquals("aaa/bbb", hierarchy.schemaOf("bbb"));

        assertEquals("aaa", idTree.getNodes().get(0).getId());//保证merge时外方放在最前面
    }

    @Test
    public void should_map_node() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode(null, "444");

        Map<String, String> idMap = Map.of("111", "aaa", "222", "bbb", "333", "ccc", "444", "ddd");
        IdTree mappedIdTree = idTree.map(idMap);
        IdTreeHierarchy hierarchy = mappedIdTree.buildHierarchy(5);
        Set<String> allIds = hierarchy.allIds();
        assertTrue(allIds.containsAll(Set.of("aaa", "bbb", "ccc", "ddd")));
        assertEquals(4, allIds.size());
        assertEquals("aaa", hierarchy.schemaOf("aaa"));
        assertEquals("aaa/bbb", hierarchy.schemaOf("bbb"));
        assertEquals("aaa/ccc", hierarchy.schemaOf("ccc"));
        assertEquals("ddd", hierarchy.schemaOf("ddd"));
    }

    @Test
    public void should_fail_map_node_if_id_map_not_complete() {
        IdTree idTree = new IdTree("111");
        idTree.addNode("111", "222");
        idTree.addNode("111", "333");
        idTree.addNode(null, "444");
        Map<String, String> idMap = Map.of("111", "aaa", "222", "bbb", "333", "ccc");
        Assertions.assertThrows(RuntimeException.class, () -> {
            idTree.map(idMap);
        });
    }

}