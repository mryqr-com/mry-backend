package com.mryqr.core.order.command;

import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.order.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateOrderResponse {
    private final String id;
    private final PaymentType paymentType;
    private final String wxPayQrUrl;
    private final String bankTransferCode;
    private final OrderPrice price;
    private final String payDescription;
    private final Instant createdAt;
}
