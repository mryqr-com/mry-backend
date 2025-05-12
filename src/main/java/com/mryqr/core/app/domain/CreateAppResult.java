package com.mryqr.core.app.domain;

import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateAppResult {
    private final App app;
    private final Group defaultGroup;
    private final GroupHierarchy groupHierarchy;
}
