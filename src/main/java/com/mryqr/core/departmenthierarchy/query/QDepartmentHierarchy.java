package com.mryqr.core.departmenthierarchy.query;

import com.mryqr.common.domain.idnode.IdTree;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QDepartmentHierarchy {
    private final IdTree idTree;
    private final List<QHierarchyDepartment> allDepartments;

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class QHierarchyDepartment {
        private final String id;
        private final String name;
        private final List<String> managers;
    }

}
