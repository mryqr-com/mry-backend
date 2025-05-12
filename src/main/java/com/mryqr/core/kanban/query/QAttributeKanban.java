package com.mryqr.core.kanban.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QAttributeKanban {
    private final List<QAttributeOptionCount> counts;
}
