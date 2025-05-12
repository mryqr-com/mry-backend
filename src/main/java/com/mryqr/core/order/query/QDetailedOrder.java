package com.mryqr.core.order.query;

import com.mryqr.core.order.domain.detail.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QDetailedOrder {
    private final String id;
    private final String description;
    private final String orderDetailType;
    private final OrderDetail orderDetail;
    private final String status;

    private final String discountedTotalPrice;
    private final String paymentType;
    private final String wxTxnId;
    private final String bankTransferCode;
    private final String bankName;
    private final String bankTransferAccountId;
    private final Instant paidAt;

    private final boolean invoiceRequested;
    private final boolean invoiceIssued;
    private final String invoiceTitle;
    private final String invoiceType;
    private final String invoiceEmail;

    private final String carrier;
    private final String deliveryOrderId;

    private final Instant createdAt;
    private final String createdBy;
    private final String creator;
}
