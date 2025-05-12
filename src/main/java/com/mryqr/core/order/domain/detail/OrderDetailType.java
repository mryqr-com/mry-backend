package com.mryqr.core.order.domain.detail;

public enum OrderDetailType {
    PLAN("购买套餐"),
    EXTRA_MEMBER("增购成员"),
    EXTRA_STORAGE("增购存储空间"),
    EXTRA_SMS("增购短信量"),
    EXTRA_VIDEO_TRAFFIC("增购视频流量"),
    PLATE_PRINTING("码牌印刷");

    private final String name;

    OrderDetailType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
