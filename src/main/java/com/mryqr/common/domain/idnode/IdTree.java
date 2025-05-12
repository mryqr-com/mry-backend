package com.mryqr.common.domain.idnode;

import com.mryqr.common.domain.idnode.exception.IdNodeLevelOverflowException;
import com.mryqr.common.domain.idnode.exception.IdNodeNotFoundException;
import com.mryqr.common.domain.idnode.exception.NodeIdFormatException;
import com.mryqr.common.domain.idnode.validation.NodeId;
import com.mryqr.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static java.util.List.of;
import static java.util.Map.copyOf;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isBlank;

@EqualsAndHashCode
@Getter(value = PACKAGE)
@NoArgsConstructor(access = PRIVATE)
public class IdTree {
    public static final String NODE_ID_SEPARATOR = "/";

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 1000)
    private List<IdNode> nodes;

    public IdTree(String initNodeId) {
        if (isBlank(initNodeId)) {
            this.nodes = new ArrayList<>(0);
        } else {
            this.nodes = new ArrayList<>(of(new IdNode(initNodeId)));
        }
    }

    public IdTree(List<IdNode> nodes) {
        requireNonNull(nodes, "Node must not be null");

        this.nodes = nodes;
    }

    public IdTreeHierarchy buildHierarchy(int maxAllowedLevel) {
        return new IdTreeHierarchy(this.nodes.stream().map(idNode -> idNode.buildHierarchy(maxAllowedLevel))
                .flatMap(map -> map.entrySet().stream())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public void addNode(String parentNodeId, String nodeId) {
        requireNonBlank(nodeId, "Node ID must not be blank.");

        if (isBlank(parentNodeId)) {
            this.nodes.add(0, new IdNode(nodeId));
            return;
        }

        IdNode parent = nodeById(parentNodeId);
        if (parent == null) {
            throw new IdNodeNotFoundException("ID node [" + parentNodeId + "] not found.");
        }

        parent.addChild(nodeId);
    }

    IdNode nodeById(String nodeId) {
        requireNonBlank(nodeId, "Node ID must not be blank.");

        for (IdNode node : nodes) {
            IdNode idNode = node.nodeById(nodeId);
            if (idNode != null) {
                return idNode;
            }
        }

        return null;
    }

    public boolean removeNode(String nodeId) {
        requireNonBlank(nodeId, "Node ID must not be blank.");

        boolean result = nodes.removeIf(idNode -> Objects.equals(nodeId, idNode.id));
        if (result) {
            return true;
        }

        for (IdNode node : nodes) {
            boolean childrenResult = node.removeChildren(nodeId);
            if (childrenResult) {
                return true;
            }
        }

        return false;
    }

    public void merge(IdTree another) {
        Set<String> selfIds = new HashSet<>(this.buildHierarchy(10).allIds());
        Set<String> anotherIds = another.buildHierarchy(10).allIds();
        selfIds.retainAll(anotherIds);
        selfIds.forEach(this::removeNode);
        this.nodes.addAll(0, another.nodes);
    }

    public IdTree map(Map<String, String> idMap) {
        if (!Objects.equals(this.buildHierarchy(10).allIds(), idMap.keySet())) {
            throw new RuntimeException("Mapped ID must contain all existing tree ids.");
        }

        List<IdNode> mappedNodes = this.nodes.stream().map(idNode -> idNode.map(idMap)).collect(toImmutableList());
        return new IdTree(mappedNodes);
    }

    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = PRIVATE)
    static class IdNode {

        @NodeId
        @NotBlank
        @Size(max = 50)
        private String id;

        @Valid
        @NotNull
        @NoNullElement
        @Size(max = 1000)
        private List<IdNode> children;

        private IdNode(String id) {
            if (id.contains(NODE_ID_SEPARATOR)) {
                throw new NodeIdFormatException("Node ID must not contain " + NODE_ID_SEPARATOR + ".");
            }

            this.id = id;
            this.children = new ArrayList<>(0);
        }

        private Map<String, String> buildHierarchy(int maxAllowedLevel) {
            Map<String, String> result = new HashMap<>();
            result.put(id, id);

            if (isEmpty(children)) {
                return result;
            }

            children.forEach(idNode -> {
                Map<String, String> hierarchy = idNode.buildHierarchy(maxAllowedLevel);
                for (Map.Entry<String, String> entry : hierarchy.entrySet()) {
                    String value = id + NODE_ID_SEPARATOR + entry.getValue();
                    int level = countMatches(value, NODE_ID_SEPARATOR) + 1;
                    if (level > maxAllowedLevel) {
                        throw new IdNodeLevelOverflowException("Max allowed level is " + maxAllowedLevel + " but actual is " + level + ".");
                    }
                    result.put(entry.getKey(), value);
                }
            });

            return copyOf(result);
        }

        private IdNode nodeById(String id) {
            if (Objects.equals(this.id, id)) {
                return this;
            }

            for (IdNode child : children) {
                IdNode idNode = child.nodeById(id);
                if (idNode != null) {
                    return idNode;
                }
            }

            return null;
        }

        private void addChild(String nodeId) {
            this.children.add(0, new IdNode(nodeId));
        }

        private boolean removeChildren(String nodeId) {
            boolean result = this.children.removeIf(idNode -> Objects.equals(nodeId, idNode.id));
            if (result) {
                return true;
            }

            for (IdNode child : children) {
                boolean childrenResult = child.removeChildren(nodeId);
                if (childrenResult) {
                    return true;
                }
            }

            return false;
        }

        private IdNode map(Map<String, String> idMap) {
            IdNode mappedNode = new IdNode(idMap.get(this.id));
            if (isEmpty(children)) {
                return mappedNode;
            }

            children.forEach(idNode -> mappedNode.children.add(idNode.map(idMap)));
            return mappedNode;
        }
    }
}
