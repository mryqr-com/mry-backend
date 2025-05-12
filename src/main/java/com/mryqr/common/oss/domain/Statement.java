package com.mryqr.common.oss.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Statement {
    private final String Effect;
    private final String Action;
    private final List<String> Resource;
}
