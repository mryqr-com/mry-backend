package com.mryqr.core.order.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ORDER_REFUND_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_REFUND_UPDATE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderRefundUpdatedEvent extends OrderUpdatedEvent {

    public OrderRefundUpdatedEvent(String orderId, User user) {
        super(ORDER_REFUND_UPDATED, orderId, user);
    }
}
