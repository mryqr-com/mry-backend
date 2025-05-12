package com.mryqr.core.order.domain;

import com.mryqr.common.wx.pay.WxPayService;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.order.domain.detail.OrderDetail;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.order.domain.PaymentType.WX_NATIVE;

@Component
@RequiredArgsConstructor
public class OrderFactory {
    private final WxPayService wxPayService;

    public Order createOrder(OrderDetail detail, PaymentType paymentType, Tenant tenant, User user) {
        Order order = new Order(detail, paymentType, tenant, user);

        if (paymentType == WX_NATIVE) {
            String wxPayOrder = wxPayService.createWxPayOrder(order);
            order.setWxPayQrUrl(wxPayOrder);
        }

        return order;
    }
}
