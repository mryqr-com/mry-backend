package com.mryqr.core.order.domain;

public enum PaymentType {
    WX_NATIVE("在线微信支付"),
    WX_TRANSFER("线下微信转账"),
    BANK_TRANSFER("银行对公转账");

    private final String name;

    PaymentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
