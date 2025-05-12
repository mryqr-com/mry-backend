package com.mryqr.core.group.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QGroupMembers {
    private final List<String> memberIds;
    private final List<String> managerIds;
}
