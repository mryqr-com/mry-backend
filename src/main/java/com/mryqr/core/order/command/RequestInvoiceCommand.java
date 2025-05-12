package com.mryqr.core.order.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.email.Email;
import com.mryqr.core.order.domain.invoice.InvoiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class RequestInvoiceCommand implements Command {

    @NotNull
    private final InvoiceType type;

    @Email
    @NotBlank
    private final String email;

}
