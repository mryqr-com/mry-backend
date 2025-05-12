package com.mryqr.core.qr.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateQrResponse {
    private final String qrId;
    private final String plateId;
    private final String groupId;
    private final String appId;
}
