package com.mryqr.common.domain.idnode;

import com.mryqr.common.domain.idnode.exception.IdNodeNotFoundException;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Maps.immutableEntry;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@EqualsAndHashCode
@NoArgsConstructor(access = PRIVATE)
public class IdTreeHierarchy {
    private Map<String, String> schemas;

    public IdTreeHierarchy(Map<String, String> schemas) {
        requireNonNull(schemas, "Hierarchy must not be null.");
        this.schemas = schemas;
    }

    public String schemaOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        String schema = schemas.get(id);
        if (isBlank(schema)) {
            throw new IdNodeNotFoundException("No ID node found for [" + id + "].");
        }
        return schema;
    }

    public int levelOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return StringUtils.countMatches(this.schemaOf(id), IdTree.NODE_ID_SEPARATOR) + 1;
    }

    public Set<String> allIds() {
        return this.schemas.keySet();
    }

    public boolean containsId(String id) {
        return this.allIds().contains(id);
    }

    public Set<String> directChildIdsUnder(String parentId) {
        if (isBlank(parentId)) {
            return this.schemas.values().stream()
                    .filter(value -> !value.contains(IdTree.NODE_ID_SEPARATOR))
                    .collect(toImmutableSet());
        }

        return this.schemas.values().stream()
                .filter(value -> value.contains(parentId + IdTree.NODE_ID_SEPARATOR))
                .map(value -> {
                    String[] split = value.split(parentId + IdTree.NODE_ID_SEPARATOR);
                    return split[1].split(IdTree.NODE_ID_SEPARATOR)[0];
                }).collect(toImmutableSet());
    }

    public Set<String> siblingIdsOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        if (isRoot(id)) {
            return allRootIds().stream()
                    .filter(aId -> !Objects.equals(aId, id))
                    .collect(toImmutableSet());
        }

        return directChildIdsUnder(parentIdOf(id))
                .stream()
                .filter(aId -> !Objects.equals(aId, id))
                .collect(toImmutableSet());
    }

    public Set<String> allRootIds() {
        return this.schemas.values().stream()
                .filter(value -> !value.contains(IdTree.NODE_ID_SEPARATOR))
                .collect(toImmutableSet());
    }

    public boolean isRoot(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return !this.schemaOf(id).contains(IdTree.NODE_ID_SEPARATOR);
    }

    public Set<String> withAllChildIdsOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return this.schemas.entrySet().stream()
                .filter(it -> it.getValue().contains(id))
                .map(Map.Entry::getKey)
                .collect(toImmutableSet());
    }

    public Set<String> allChildIdsOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return this.schemas.entrySet().stream()
                .filter(it -> it.getValue().contains(id) && !it.getKey().equals(id))
                .map(Map.Entry::getKey)
                .collect(toImmutableSet());
    }

    public Set<String> withAllParentIdsOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return Set.of(this.schemaOf(id).split(IdTree.NODE_ID_SEPARATOR));
    }

    public Set<String> allParentIdsOf(String id) {
        requireNonBlank(id, "Node ID must not be null.");

        return stream(this.schemaOf(id).split(IdTree.NODE_ID_SEPARATOR))
                .filter(it -> !Objects.equals(it, id))
                .collect(toImmutableSet());
    }

    public Map<String, String> fullNames(Map<String, String> allNames) {
        requireNonNull(allNames, "Provided names must not be null.");

        return this.schemas.entrySet().stream().map(entry -> {
            String names = stream(entry.getValue().split(IdTree.NODE_ID_SEPARATOR)).map(groupId -> {
                String name = allNames.get(groupId);
                if (isBlank(name)) {
                    throw new RuntimeException("No  name found for id[" + groupId + "].");
                }
                return name;
            }).collect(joining(IdTree.NODE_ID_SEPARATOR));
            return immutableEntry(entry.getKey(), names);
        }).collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String parentIdOf(String id) {
        String[] ids = this.schemaOf(id).split(IdTree.NODE_ID_SEPARATOR);
        if (ids.length <= 1) {
            return null;
        }
        return ids[ids.length - 2];
    }
}
