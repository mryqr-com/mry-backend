package com.mryqr.core.order.domain.delivery;

public enum Carrier {
    EMS("EMS", "EMS"),
    SHUN_FENG("顺丰", "SFEXPRESS"),
    YUAN_TONG("圆通", "YTO"),
    ZHONG_TONG("中通", "ZTO"),
    ZHONG_TONG_56("中通快运", "ZTO56"),
    SHEN_TONG("申通", "STO"),
    YUN_DA("韵达", "YUNDA"),
    YUN_DA_56("韵达快运", "YUNDA56");

    private final String name;
    private final String type;

    Carrier(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
