package com.mryqr.common.wx.pay.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class WxNotifyResult {
    private String wxTxnId;
    private String orderId;
    private Instant paidAt;
}
