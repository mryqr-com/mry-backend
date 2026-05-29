package com.mryqr.common.wx.pay;

import com.mryqr.common.wx.pay.notify.WxNotifyResult;
import com.mryqr.core.order.domain.Order;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static com.mryqr.common.utils.UuidGenerator.newShortUuid;

@Component
@AllArgsConstructor
public class FakeWxPayService implements WxPayService {

    @Override
    public String createWxPayOrder(Order order) {
        return "https://www.mryqr.com/" + newShortUuid();
    }

    @Override
    public boolean validateCallbackSignature(HttpHeaders headers, String body) {
        return true;
    }

    @Override
    public WxNotifyResult extractResultFromCallback(String body) {
        return WxNotifyResult.builder()
                .paidAt(Instant.now())
                .wxTxnId(newShortUuid())
                .orderId(Order.newOrderId())
                .build();
    }

}
