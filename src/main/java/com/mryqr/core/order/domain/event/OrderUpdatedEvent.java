package com.mryqr.core.order.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.DomainEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class OrderUpdatedEvent extends DomainEvent {
    private String orderId;

    public OrderUpdatedEvent(DomainEventType type, String orderId, User user) {
        super(type, user);
        this.orderId = orderId;
    }
}
