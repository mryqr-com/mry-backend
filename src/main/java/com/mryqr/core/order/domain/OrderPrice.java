package com.mryqr.core.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class OrderPrice {
    private final String originalUpgradePrice;
    private final String originalRenewalPrice;
    private final String originalTotalPrice;

    private final String deliveryFee;

    private final String discount;
    private final String discountOffsetPrice;
    private final String discountedTotalPrice;

}
