package com.mryqr.core.order.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ORDER_INVOICE_ISSUED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ORDER_INVOICE_ISSUED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class OrderInvoiceIssuedEvent extends OrderUpdatedEvent {

    public OrderInvoiceIssuedEvent(String orderId, User user) {
        super(ORDER_INVOICE_ISSUED, orderId, user);
    }
}
