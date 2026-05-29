package com.mryqr.common.wx.pay;

import com.mryqr.common.wx.pay.notify.WxNotifyResult;
import com.mryqr.core.order.domain.Order;
import org.springframework.http.HttpHeaders;

public interface WxPayService {

    String createWxPayOrder(Order order);

    boolean validateCallbackSignature(HttpHeaders headers, String body);

    WxNotifyResult extractResultFromCallback(String body);

}
