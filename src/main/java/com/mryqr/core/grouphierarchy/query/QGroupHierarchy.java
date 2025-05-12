package com.mryqr.core.grouphierarchy.query;

import com.mryqr.core.common.domain.idnode.IdTree;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QGroupHierarchy {
    private final IdTree idTree;
    private final List<QHierarchyGroup> allGroups;
    private final boolean sync;

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QHierarchyGroup {
        private final String id;
        private final String name;
        private final boolean active;
        private final boolean archived;
        private final boolean sync;
    }

}
