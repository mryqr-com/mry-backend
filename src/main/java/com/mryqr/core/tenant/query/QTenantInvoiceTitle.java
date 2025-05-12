package com.mryqr.core.tenant.query;

import com.mryqr.common.domain.invoice.InvoiceTitle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTenantInvoiceTitle {
    private final InvoiceTitle title;
}
