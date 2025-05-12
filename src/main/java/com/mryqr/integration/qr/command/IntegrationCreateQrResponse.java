package com.mryqr.integration.qr.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationCreateQrResponse {
    private final String qrId;
    private final String plateId;
    private final String groupId;
    private final String appId;
}
