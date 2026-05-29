package com.mryqr.common.wx.pay.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class WxPayNotifyRequest {
    private final String id;
    private final String create_time;
    private final String resource_type;
    private final String event_type;
    private final String summary;
    private final Resource resource;

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class Resource {
        private final String original_type;
        private final String algorithm;
        private final String ciphertext;
        private final String associated_data;
        private final String nonce;
    }
}
