package com.mryqr.core.order.domain.invoice;

public enum InvoiceType {
    PERSONAL("个人"),
    VAT_NORMAL("增值税普通发票"),
    VAT_SPECIAL("增值税专用发票");

    private final String name;

    InvoiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
