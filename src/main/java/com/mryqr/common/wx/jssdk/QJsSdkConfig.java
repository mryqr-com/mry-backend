package com.mryqr.common.wx.jssdk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QJsSdkConfig {
    private final String url;
    private final String appId;
    private final long timestamp;
    private final String nonce;
    private final String signature;
}
