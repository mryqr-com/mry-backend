package com.mryqr.common.wx.pay;

import lombok.Getter;

@Getter
public final class WxOrderRequest {
    private final String mchid;
    private final String out_trade_no;
    private final String appid;
    private final String description;
    private final String notify_url;
    private final Amount amount;

    public WxOrderRequest(String merchantId,
                          String outTradeNo,
                          String appid,
                          String description,
                          String notifyUrl,
                          int total) {
        this.mchid = merchantId;
        this.out_trade_no = outTradeNo;
        this.appid = appid;
        this.description = description;
        this.notify_url = notifyUrl;
        this.amount = new Amount(total);
    }

    @Getter
    static final class Amount {
        private final int total;
        private final String currency = "CNY";

        public Amount(int total) {
            this.total = total;
        }
    }
}
