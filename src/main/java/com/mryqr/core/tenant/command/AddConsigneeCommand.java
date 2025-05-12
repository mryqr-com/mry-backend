package com.mryqr.core.tenant.command;

import com.mryqr.common.utils.Command;
import com.mryqr.core.order.domain.delivery.Consignee;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AddConsigneeCommand implements Command {

    @Valid
    @NotNull
    private final Consignee consignee;
}
