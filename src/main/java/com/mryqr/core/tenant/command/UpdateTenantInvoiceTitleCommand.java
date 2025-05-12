package com.mryqr.core.tenant.command;

import com.mryqr.core.common.domain.invoice.InvoiceTitle;
import com.mryqr.core.common.utils.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateTenantInvoiceTitleCommand implements Command {

    @Valid
    @NotNull
    private final InvoiceTitle title;

}
