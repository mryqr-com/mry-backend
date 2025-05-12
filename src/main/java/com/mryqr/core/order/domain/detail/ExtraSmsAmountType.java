package com.mryqr.core.order.domain.detail;

public enum ExtraSmsAmountType {
    ONE_K(1000, 80),
    TWO_K(2000, 160),
    FIVE_K(5000, 350),
    TEN_K(10000, 600),
    TWENTY_K(20000, 1000),
    FIFTY_K(50000, 2500);

    private final int amount;
    private final int price;

    ExtraSmsAmountType(int amount, int price) {
        this.amount = amount;
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }
}
