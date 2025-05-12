package com.mryqr.core.order.query;

import com.mryqr.core.order.domain.OrderStatus;
import com.mryqr.core.order.domain.detail.OrderDetailType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListOrder {
    private final String id;
    private final OrderDetailType orderDetailTypeEnum;
    private final String orderDetailType;
    private final String status;
    private final OrderStatus statusEnum;
    private final String description;

    private final String paidPrice;
    private final String paymentType;
    private final Instant paidAt;

    private final Instant createdAt;
    private final boolean invoiceRequested;
    private final boolean invoiceIssued;
}
