package com.mryqr.common.domain.display;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class EmailedMember {
    private String id;
    private String name;
    private String email;
}
