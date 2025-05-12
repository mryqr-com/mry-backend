package com.mryqr.core.order.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ORDER_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderCreatedEvent extends DomainEvent {
    private String orderId;

    public OrderCreatedEvent(String orderId, User user) {
        super(ORDER_CREATED, user);
        this.orderId = orderId;
    }
}
