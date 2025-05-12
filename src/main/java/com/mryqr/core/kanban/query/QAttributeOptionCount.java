package com.mryqr.core.kanban.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAttributeOptionCount {
    private final String optionId;
    private final int count;
}
