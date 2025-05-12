package com.mryqr.core.order.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.order.domain.PaymentType;
import com.mryqr.core.order.domain.detail.OrderDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateOrderCommand implements Command {
    @Valid
    @NotNull
    private final OrderDetail detail;

    @NotNull
    private final PaymentType paymentType;
}
