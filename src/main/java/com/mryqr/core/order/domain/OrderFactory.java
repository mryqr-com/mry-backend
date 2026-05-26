package com.mryqr.core.order.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.order.domain.detail.OrderDetail;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderFactory {
    public Order createOrder(OrderDetail detail, PaymentType paymentType, Tenant tenant, User user) {
        return new Order(detail, paymentType, tenant, user);
    }
}
