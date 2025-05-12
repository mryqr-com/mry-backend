package com.mryqr.core.order.query;

import com.mryqr.core.order.domain.OrderPrice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QPriceQuotation {
    private final OrderPrice price;
}
