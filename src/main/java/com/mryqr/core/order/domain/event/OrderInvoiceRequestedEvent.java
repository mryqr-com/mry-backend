package com.mryqr.core.order.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ORDER_INVOICE_REQUESTED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_INVOICE_REQUESTED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderInvoiceRequestedEvent extends OrderUpdatedEvent {

    public OrderInvoiceRequestedEvent(String orderId, User user) {
        super(ORDER_INVOICE_REQUESTED, orderId, user);
    }
}
