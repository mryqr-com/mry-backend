package com.mryqr.core.order.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ORDER_WX_PAY_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_WX_PAY_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderWxPayUpdatedEvent extends OrderUpdatedEvent {

    public OrderWxPayUpdatedEvent(String orderId, User user) {
        super(ORDER_WX_PAY_UPDATED, orderId, user);
    }
}
