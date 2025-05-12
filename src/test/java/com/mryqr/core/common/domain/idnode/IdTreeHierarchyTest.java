package com.mryqr.core.common.domain.idnode;

import com.mryqr.common.domain.idnode.IdTreeHierarchy;
import com.mryqr.common.domain.idnode.exception.IdNodeNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class IdTreeHierarchyTest {

    @Test
    public void should_get_schema_for_id() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals("111", hierarchy.schemaOf("111"));
        assertEquals("222/333", hierarchy.schemaOf("333"));

        assertThrows(IdNodeNotFoundException.class, () -> hierarchy.schemaOf("whatever"));
    }

    @Test
    public void should_calculate_level() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(1, hierarchy.levelOf("111"));
        assertEquals(1, hierarchy.levelOf("222"));
        assertEquals(2, hierarchy.levelOf("333"));
        assertEquals(3, hierarchy.levelOf("444"));
    }

    @Test
    public void should_return_all_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of("111", "222", "333", "444"), hierarchy.allIds());
    }

    @Test
    public void should_calculate_contains() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertTrue(hierarchy.containsId("111"));
        assertTrue(hierarchy.containsId("222"));
        assertTrue(hierarchy.containsId("333"));
        assertTrue(hierarchy.containsId("444"));
    }

    @Test
    public void should_return_direct_children() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of(), hierarchy.directChildIdsUnder("111"));
        assertEquals(Set.of("333"), hierarchy.directChildIdsUnder("222"));
        assertEquals(Set.of("444", "555"), hierarchy.directChildIdsUnder("333"));
    }

    @Test
    public void should_return_sibling_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of("222"), hierarchy.siblingIdsOf("111"));
        assertEquals(Set.of("111"), hierarchy.siblingIdsOf("222"));
        assertEquals(Set.of(), hierarchy.siblingIdsOf("333"));
        assertEquals(Set.of("555"), hierarchy.siblingIdsOf("444"));
        assertEquals(Set.of("444"), hierarchy.siblingIdsOf("555"));
    }

    @Test
    public void should_return_all_root_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of("111", "222"), hierarchy.allRootIds());
    }

    @Test
    public void should_return_is_root() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertTrue(hierarchy.isRoot("111"));
        assertTrue(hierarchy.isRoot("222"));
        assertFalse(hierarchy.isRoot("333"));
        assertFalse(hierarchy.isRoot("444"));
        assertFalse(hierarchy.isRoot("555"));
    }

    @Test
    public void should_return_all_with_sub_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of("111"), hierarchy.withAllChildIdsOf("111"));
        assertEquals(Set.of("222", "333", "444", "555"), hierarchy.withAllChildIdsOf("222"));
        assertEquals(Set.of("333", "444", "555"), hierarchy.withAllChildIdsOf("333"));
        assertEquals(Set.of("444"), hierarchy.withAllChildIdsOf("444"));
        assertEquals(Set.of("555"), hierarchy.withAllChildIdsOf("555"));
    }

    @Test
    public void should_return_all_sub_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of(), hierarchy.allChildIdsOf("111"));
        assertEquals(Set.of("333", "444", "555"), hierarchy.allChildIdsOf("222"));
        assertEquals(Set.of("444", "555"), hierarchy.allChildIdsOf("333"));
        assertEquals(Set.of(), hierarchy.allChildIdsOf("444"));
        assertEquals(Set.of(), hierarchy.allChildIdsOf("555"));
    }

    @Test
    public void should_return_with_all_parent_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of("111"), hierarchy.withAllParentIdsOf("111"));
        assertEquals(Set.of("222"), hierarchy.withAllParentIdsOf("222"));
        assertEquals(Set.of("222", "333"), hierarchy.withAllParentIdsOf("333"));
        assertEquals(Set.of("222", "333", "444"), hierarchy.withAllParentIdsOf("444"));
        assertEquals(Set.of("222", "333", "555"), hierarchy.withAllParentIdsOf("555"));
    }

    @Test
    public void should_return_all_parent_ids() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        assertEquals(Set.of(), hierarchy.allParentIdsOf("111"));
        assertEquals(Set.of(), hierarchy.allParentIdsOf("222"));
        assertEquals(Set.of("222"), hierarchy.allParentIdsOf("333"));
        assertEquals(Set.of("222", "333"), hierarchy.allParentIdsOf("444"));
        assertEquals(Set.of("222", "333"), hierarchy.allParentIdsOf("555"));
    }

    @Test
    public void should_return_full_names() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        Map<String, String> names = Map.of(
                "111", "aaa",
                "222", "bbb",
                "333", "ccc",
                "444", "ddd",
                "555", "eee"
        );
        Map<String, String> fullNames = hierarchy.fullNames(names);
        assertEquals("aaa", fullNames.get("111"));
        assertEquals("bbb", fullNames.get("222"));
        assertEquals("bbb/ccc", fullNames.get("333"));
        assertEquals("bbb/ccc/ddd", fullNames.get("444"));
        assertEquals("bbb/ccc/eee", fullNames.get("555"));
    }

    @Test
    public void should_fail_return_full_names_if_name_not_provided() {
        Map<String, String> schemas = Map.of(
                "111", "111",
                "222", "222",
                "333", "222/333",
                "444", "222/333/444",
                "555", "222/333/555"
        );

        IdTreeHierarchy hierarchy = new IdTreeHierarchy(schemas);
        Map<String, String> names = Map.of(
                "111", "aaa",
                "222", "bbb",
                "333", "ccc",
                "444", "ddd"
        );

        assertThrows(RuntimeException.class, () -> hierarchy.fullNames(names));
    }

}