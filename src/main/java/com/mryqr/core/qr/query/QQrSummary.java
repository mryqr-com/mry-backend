package com.mryqr.core.qr.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QQrSummary {
    private String id;
    private String name;
    private String plateId;
    private String appId;
    private String groupId;
    private boolean template;
}
