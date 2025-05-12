package com.mryqr.core.order.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ORDER_WX_TRANSFER_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_WX_TRANSFER_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderWxTransferUpdatedEvent extends OrderUpdatedEvent {

    public OrderWxTransferUpdatedEvent(String orderId, User user) {
        super(ORDER_WX_TRANSFER_UPDATED, orderId, user);
    }
}
