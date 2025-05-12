package com.mryqr.core.common.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ReturnId {
    private final String id;

    public static ReturnId returnId(String id) {
        return ReturnId.builder().id(id).build();
    }

    @Override
    public String toString() {
        return id;
    }
}
