package com.mryqr.core.register.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class RegisterResponse {
    private String tenantId;
    private String memberId;
}
