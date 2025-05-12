package com.mryqr.core.kanban.query;


import com.mryqr.common.utils.Query;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.attribute.AttributeId;
import com.mryqr.common.validation.id.group.GroupId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class FetchKanbanQuery implements Query {
    @AppId
    @NotBlank
    private final String appId;

    @GroupId
    private final String groupId;

    @NotBlank
    @AttributeId
    private final String attributeId;
}
